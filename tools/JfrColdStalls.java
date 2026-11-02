import java.nio.file.*;
import java.time.*;
import java.util.*;
import jdk.jfr.consumer.*;

public class JfrColdStalls {
    static String name(RecordedFrame f){var m=f.getMethod();return m.getType().getName()+"."+m.getName();}
    public static void main(String[] args)throws Exception{
        var file=Path.of(args[0]);Instant stable=null;
        try(var r=new RecordingFile(file)){while(r.hasMoreEvents()){var e=r.readEvent();if(e.getEventType().getName().equals("innerdimension.OpenDomainPhase")&&e.getString("phase").equals("stable"))stable=e.getStartTime();}}
        var out=new StringBuilder();Map<String,Integer> nativeCounts=new HashMap<>();Map<String,String> nativeStacks=new HashMap<>();
        try(var r=new RecordingFile(file)){while(r.hasMoreEvents()){
            var e=r.readEvent();String type=e.getEventType().getName();
            if(type.equals("innerdimension.OpenDomainPhase"))out.append("PHASE ").append(e.getStartTime()).append(' ').append(e.getString("phase")).append('\n');
            if(type.equals("jdk.GarbageCollection"))out.append("GC ").append(e.getStartTime()).append(" pause_ms=").append(e.getDuration("sumOfPauses").toNanos()/1e6).append(" cause=").append(e.getString("cause")).append('\n');
            if((type.equals("jdk.ExecuteVMOperation")||type.equals("jdk.SafepointBegin")||type.equals("jdk.GCPhasePause")||type.equals("jdk.Compilation"))&&e.getDuration().toMillis()>=100){
                out.append(type).append(' ').append(e.getStartTime()).append(" duration_ms=").append(e.getDuration().toNanos()/1e6);
                for(String field:List.of("operation","name","safepoint","safepointId"))if(e.hasField(field))out.append(' ').append(field).append('=').append((Object)e.getValue(field));
                if(e.hasField("caller")){var caller=e.getThread("caller");if(caller!=null)out.append(" caller=").append(caller.getJavaName());}
                out.append('\n');
            }
            if(type.equals("jdk.NativeMethodSample")&&(stable==null||e.getStartTime().isBefore(stable))&&e.getThread("sampledThread").getJavaName().equals("Render thread")){
                var frames=e.getStackTrace().getFrames();if(frames.isEmpty())continue;
                String key=frames.stream().limit(6).map(JfrColdStalls::name).reduce((a,b)->a+" > "+b).orElse("");
                nativeCounts.merge(key,1,Integer::sum);StringBuilder stack=new StringBuilder();for(var frame:frames)stack.append("  ").append(name(frame)).append('\n');nativeStacks.putIfAbsent(key,stack.toString());
            }
        }}
        out.append("\nCOLD NATIVE RENDER STACKS\n");nativeCounts.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).limit(6).forEach(e->out.append("samples=").append(e.getValue()).append('\n').append(nativeStacks.get(e.getKey())));
        var dest=file.resolveSibling(file.getFileName().toString().replace(".jfr","-cold-stalls.txt"));Files.writeString(dest,out);System.out.println(dest);
    }
}
