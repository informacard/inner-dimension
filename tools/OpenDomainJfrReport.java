import com.google.gson.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import jdk.jfr.consumer.*;

/** Streams JFR directly; never expands a small recording into hundreds of MB of JSON. */
public final class OpenDomainJfrReport {
    private static final class Samples {
        int javaCount,nativeCount;
        final Map<String,Integer> javaMethods=new HashMap<>(),nativeMethods=new HashMap<>();
    }
    private static String method(RecordedFrame frame){
        var method=frame.getMethod();return method.getType().getName()+"."+method.getName();
    }
    private static String thread(RecordedEvent event,String field){
        var thread=event.getThread(field);return thread==null?"unknown":thread.getJavaName();
    }
    private static String escaped(String value){return value.replace("|","\\|").replace("\n"," ");}
    private static String number(double value){return String.format(Locale.ROOT,"%.3f",value);}
    private static void appendSamples(StringBuilder out,String label,int count,Map<String,Integer> methods){
        out.append("\n").append(label).append("：").append(count).append(" 个样本。以下为调用栈包含次数，同一栈可能含多个方法，百分比不能相加。\n\n");
        out.append("| 方法 | 样本 | 占该类样本 |\n|---|---:|---:|\n");
        methods.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey())).limit(20).forEach(entry->
            out.append("| ").append(escaped(entry.getKey())).append(" | ").append(entry.getValue()).append(" | ").append(number(100.0*entry.getValue()/Math.max(1,count))).append("% |\n"));
    }
    private static void appendTimers(StringBuilder out,Path recording)throws Exception{
        Path metrics=recording.resolveSibling(recording.getFileName().toString().replace(".jfr","-metrics.json"));
        if(!Files.isRegularFile(metrics))return;
        var root=JsonParser.parseString(Files.readString(metrics)).getAsJsonObject();
        out.append("\n## 插桩计时\n\n计时包含嵌套调用，不能直接相加。CLIENT_FRAME 包含客户端 tick、显示交换与帧率限制；CPU 用时排除线程等待。\n\n");
        out.append("阶段时长：").append(root.get("phase_seconds")).append(" 秒。\n\n");
        out.append("| 阶段 | 线程 | 指标 | 次数/数量 | 总 ms | 平均 ms | 最大 ms | CPU 平均 ms |\n|---|---|---|---:|---:|---:|---:|---:|\n");
        for(var item:root.getAsJsonArray("metrics")){
            var row=item.getAsJsonObject();
            out.append("| ").append(row.get("phase").getAsString()).append(" | ").append(row.get("thread").getAsString()).append(" | ").append(row.get("metric").getAsString());
            out.append(" | ").append(row.get("count").getAsLong()).append(" | ").append(number(row.get("total_ms").getAsDouble())).append(" | ").append(number(row.get("mean_ms").getAsDouble()));
            out.append(" | ").append(number(row.get("max_ms").getAsDouble())).append(" | ").append(row.has("cpu_mean_ms")?number(row.get("cpu_mean_ms").getAsDouble()):"—").append(" |\n");
        }
        out.append("\n实际 VertexConsumer：").append(root.get("vertex_consumers")).append("。\n");
    }
    public static void main(String[] args)throws Exception{
        if(args.length<1||args.length>2)throw new IllegalArgumentException("Usage: OpenDomainJfrReport recording.jfr [output.md]");
        Path source=Path.of(args[0]).toAbsolutePath();
        Path destination=args.length==2?Path.of(args[1]):source.resolveSibling(source.getFileName().toString().replace(".jfr","-analysis.md"));
        Instant first=null,last=null,stable=null,finished=null;
        try(var recording=new RecordingFile(source)){
            while(recording.hasMoreEvents()){
                var event=recording.readEvent();Instant time=event.getStartTime();
                if(first==null||time.isBefore(first))first=time;if(last==null||time.isAfter(last))last=time;
                if(event.getEventType().getName().equals("innerdimension.OpenDomainPhase")){
                    String phase=event.getString("phase");
                    if(phase.equals("stable")&&(stable==null||time.isBefore(stable)))stable=time;
                    if(phase.equals("finished")&&(finished==null||time.isAfter(finished)))finished=time;
                }
            }
        }
        boolean marked=stable!=null&&finished!=null;
        Instant from=marked?stable.plusSeconds(1):first,to=marked?finished:last;
        Map<String,Samples> samples=new TreeMap<>();Map<String,Double> locks=new HashMap<>();Map<String,Integer> allocations=new HashMap<>();
        int collections=0;double gcMillis=0,maxGcMillis=0;int javaSamples=0,nativeSamples=0;
        if(from!=null&&to!=null&&to.isAfter(from))try(var recording=new RecordingFile(source)){
            while(recording.hasMoreEvents()){
                var event=recording.readEvent();Instant time=event.getStartTime();if(time.isBefore(from)||time.isAfter(to))continue;
                String type=event.getEventType().getName();
                if(type.equals("jdk.ExecutionSample")||type.equals("jdk.NativeMethodSample")){
                    boolean nativeSample=type.equals("jdk.NativeMethodSample");
                    Samples group=samples.computeIfAbsent(thread(event,"sampledThread"),ignored->new Samples());
                    if(nativeSample){group.nativeCount++;nativeSamples++;}else{group.javaCount++;javaSamples++;}
                    var stack=event.getStackTrace();if(stack==null)continue;
                    Set<String> methods=new HashSet<>();for(var frame:stack.getFrames())methods.add(method(frame));
                    for(String method:methods)(nativeSample?group.nativeMethods:group.javaMethods).merge(method,1,Integer::sum);
                }else if(type.equals("jdk.JavaMonitorEnter")){
                    var stack=event.getStackTrace();String location=stack==null||stack.getFrames().isEmpty()?"unknown":method(stack.getFrames().getFirst());
                    locks.merge(thread(event,"eventThread")+" / "+location,event.getDuration().toNanos()/1e6,Double::sum);
                }else if(type.equals("jdk.GarbageCollection")){
                    collections++;double pause=event.getDuration("sumOfPauses").toNanos()/1e6;gcMillis+=pause;maxGcMillis=Math.max(maxGcMillis,pause);
                }else if(type.equals("jdk.ObjectAllocationSample")){
                    allocations.merge(event.getClass("objectClass").getName(),1,Integer::sum);
                }
            }
        }
        var out=new StringBuilder("# 开放领域 JFR 摘要\n\n输入：`").append(source).append("`。\n\n");
        if(marked)out.append("已找到领域稳定与结束标记。只统计稳定后第 1 秒到采样结束，避开展开及最初一秒的稳定转换。\n\n");
        else out.append("**没有完整领域阶段标记。这是整段参考采样，不能据此断言领域展开时的瓶颈。**\n\n");
        out.append("窗口：").append(from).append(" 至 ").append(to).append("。Java 执行样本 ").append(javaSamples).append("，原生方法样本 ").append(nativeSamples).append("。\n");
        out.append("\nGC：").append(collections).append(" 次，暂停合计 ").append(number(gcMillis)).append(" ms，单次暂停合计最大 ").append(number(maxGcMillis)).append(" ms。\n");
        appendTimers(out,source);
        for(var entry:samples.entrySet()){
            out.append("\n## ").append(escaped(entry.getKey())).append("\n");
            if(entry.getValue().javaCount>0)appendSamples(out,"Java 执行",entry.getValue().javaCount,entry.getValue().javaMethods);
            if(entry.getValue().nativeCount>0)appendSamples(out,"原生方法（也可能包含等待，不能当作 CPU 占用）",entry.getValue().nativeCount,entry.getValue().nativeMethods);
        }
        out.append("\n## 超过 JFR 阈值的锁等待\n\n只包含 JFR 捕获的较长等待，不代表所有锁开销。\n\n| 线程 / 栈顶 | 等待总 ms |\n|---|---:|\n");
        locks.entrySet().stream().sorted(Map.Entry.<String,Double>comparingByValue().reversed()).limit(20).forEach(entry->out.append("| ").append(escaped(entry.getKey())).append(" | ").append(number(entry.getValue())).append(" |\n"));
        out.append("\n## 对象分配样本\n\n这里只列抽样次数。采样权重可能跨越窗口或线程寿命，不能换算成实际 MB/s 或内存占用。\n\n| 类型 | 样本数 |\n|---|---:|\n");
        allocations.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).limit(20).forEach(entry->out.append("| ").append(escaped(entry.getKey())).append(" | ").append(entry.getValue()).append(" |\n"));
        Files.writeString(destination,out);System.out.println("Saved report: "+destination);
    }
}
