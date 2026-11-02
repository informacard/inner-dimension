import java.nio.file.*;
import java.time.*;
import java.util.*;
import jdk.jfr.consumer.*;

public final class JfrHotStacks {
    static String name(RecordedFrame frame){var m=frame.getMethod();return m.getType().getName()+"."+m.getName();}
    public static void main(String[] args)throws Exception{
        Path input=Path.of(args[0]);Instant stable=null,finish=null;
        try(var file=new RecordingFile(input)){while(file.hasMoreEvents()){
            var e=file.readEvent();if(!e.getEventType().getName().equals("innerdimension.OpenDomainPhase"))continue;
            if(e.getString("phase").equals("stable"))stable=e.getStartTime();
            if(e.getString("phase").equals("finished"))finish=e.getStartTime();
        }}
        boolean expansion=args.length>1&&args[1].equals("expansion");
        Instant from=expansion||stable==null?Instant.MIN:stable.plusSeconds(1),to=expansion&&stable!=null?stable:finish==null?Instant.MAX:finish;
        Map<String,Map<String,Integer>> methods=new TreeMap<>(),stacks=new TreeMap<>(),leaves=new TreeMap<>();
        Map<String,String> examples=new HashMap<>();Map<String,Integer> totals=new HashMap<>();
        try(var file=new RecordingFile(input)){while(file.hasMoreEvents()){
            var e=file.readEvent();if(!e.getEventType().getName().equals("jdk.ExecutionSample")||e.getStartTime().isBefore(from)||e.getStartTime().isAfter(to))continue;
            String thread=e.getThread("sampledThread").getJavaName();if(!thread.equals("Render thread")&&!thread.equals("Server thread"))continue;
            var trace=e.getStackTrace();if(trace==null||trace.getFrames().isEmpty())continue;
            totals.merge(thread,1,Integer::sum);Set<String> seen=new HashSet<>();StringBuilder key=new StringBuilder(),example=new StringBuilder();
            for(int i=0;i<trace.getFrames().size();i++){
                var frame=trace.getFrames().get(i);String method=name(frame);seen.add(method);if(i<6)key.append(method).append(" > ");
                example.append("  ").append(method).append(':').append(frame.getLineNumber()).append('\n');
            }
            String k=key.toString();stacks.computeIfAbsent(thread,x->new HashMap<>()).merge(k,1,Integer::sum);examples.putIfAbsent(thread+k,example.toString());
            leaves.computeIfAbsent(thread,x->new HashMap<>()).merge(name(trace.getFrames().getFirst()),1,Integer::sum);
            for(String method:seen)methods.computeIfAbsent(thread,x->new HashMap<>()).merge(method,1,Integer::sum);
        }}
        StringBuilder out=new StringBuilder("Window: "+from+" -- "+to+"\n");
        for(String thread:methods.keySet()){
            out.append("\nTHREAD ").append(thread).append(" TOTAL ").append(totals.get(thread)).append("\nINCLUSIVE METHODS\n");
            methods.get(thread).entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).limit(100).forEach(e->out.append(e.getValue()).append(" ").append(e.getKey()).append('\n'));
            out.append("\nLEAF METHODS\n");leaves.get(thread).entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).limit(25).forEach(e->out.append(e.getValue()).append(" ").append(e.getKey()).append('\n'));
            out.append("\nTOP STACKS\n");stacks.get(thread).entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).limit(8).forEach(e->out.append("SAMPLES ").append(e.getValue()).append('\n').append(examples.get(thread+e.getKey())).append('\n'));
        }
        Path output=input.resolveSibling(input.getFileName().toString().replace(".jfr",expansion?"-expansion-hotstacks.txt":"-hotstacks.txt"));Files.writeString(output,out);System.out.println(output);
    }
}
