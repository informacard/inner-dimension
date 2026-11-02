package com.innerdimension.projection;

import com.google.gson.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

/** Exercises the real JFR recorder in a standalone JVM, without launching Minecraft. */
public final class OpenDomainPerformanceRegression {
    private static int checks;
    private static volatile long sink;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static void await(BooleanSupplier condition,String message)throws Exception{
        long deadline=System.nanoTime()+12_000_000_000L;
        while(!condition.getAsBoolean()&&System.nanoTime()<deadline)LockSupport.parkNanos(20_000_000L);
        check(condition.getAsBoolean(),message);
    }
    private static long files(Path directory,String suffix){
        try(var paths=Files.list(directory)){return paths.filter(p->p.toString().endsWith(suffix)).count();}
        catch(Exception failure){throw new RuntimeException(failure);}
    }
    private static JsonObject firstMetrics(Path directory)throws Exception{
        try(var paths=Files.list(directory)){
            Path first=paths.filter(p->p.toString().endsWith("-metrics.json")).sorted().findFirst().orElseThrow();
            return JsonParser.parseString(Files.readString(first)).getAsJsonObject();
        }
    }
    private static JsonObject metric(JsonObject root,String phase,String thread,String name){
        for(var entry:root.getAsJsonArray("metrics")){
            var row=entry.getAsJsonObject();
            if(row.get("phase").getAsString().equals(phase)&&row.get("thread").getAsString().equals(thread)&&row.get("metric").getAsString().equals(name))return row;
        }
        throw new AssertionError("Missing metric: "+phase+" "+thread+" "+name);
    }
    public static void main(String[] arguments)throws Exception{
        var loader=OpenDomainPerformanceRegression.class.getClassLoader();
        var renderer=Class.forName("net.minecraft.class_757",false,loader);
        var ticks=Class.forName("net.minecraft.class_9779",false,loader);
        check(renderer.getDeclaredMethod("method_3192",ticks,boolean.class)!=null,"render timing target matches this Minecraft build");
        check(Class.forName("net.minecraft.server.MinecraftServer",false,loader).getDeclaredMethod("method_3748",BooleanSupplier.class)!=null,"server timing target matches this Minecraft build");
        check(Class.forName("net.minecraft.class_310",false,loader).getDeclaredMethod("method_1523",boolean.class)!=null,"frame timing target matches this Minecraft build");
        Path directory=Files.createTempDirectory(Path.of(arguments[0]),"recorder-test-");
        UUID owner=UUID.randomUUID();String originalName=Thread.currentThread().getName();
        check(OpenDomainPerformance.begin()==0,"disabled timer has no clock work");
        check(OpenDomainPerformance.span(OpenDomainPerformance.Metric.CLIENT_FRAME)==null,"disabled envelope allocates no span");
        long firstStart=System.nanoTime();
        OpenDomainPerformance.start(directory,owner,Duration.ofSeconds(6));
        await(()->FlightRecorder.getFlightRecorder().getRecordings().stream().anyMatch(r->r.getName().equals("Inner Dimension open-domain observation")&&r.getState()==RecordingState.RUNNING),"JFR actually started");
        try{
            Thread.currentThread().setName("Render thread");
            var span=OpenDomainPerformance.span(OpenDomainPerformance.Metric.CLIENT_FRAME);
            long value=0;for(int i=0;i<2_000_000;i++)value=value*31+i;sink=value;
            OpenDomainPerformance.end(span);
            OpenDomainPerformance.count(OpenDomainPerformance.Metric.VERTICES,1234);
            OpenDomainPerformance.consumer(OpenDomainPerformanceRegression.class);
            OpenDomainPerformance.stable(UUID.randomUUID());
            OpenDomainPerformance.stable(owner);
            long transition=OpenDomainPerformance.begin();
            OpenDomainPerformance.end(OpenDomainPerformance.Metric.MESH_BUILD,transition);
            LockSupport.parkNanos(1_100_000_000L);
            Thread.currentThread().setName("Server thread");
            OpenDomainPerformance.count(OpenDomainPerformance.Metric.MIRROR_CHANGES,7);
            OpenDomainPerformance.finish(UUID.randomUUID());
            check(OpenDomainPerformance.begin()!=0,"another owner's close cannot stop recording");
            OpenDomainPerformance.finish(owner);
            await(()->files(directory,"-metrics.json")==1,"timing report written after manual finish");
            var root=firstMetrics(directory);
            check(root.get("stable_seen").getAsBoolean(),"stable phase recorded");
            check(root.get("jfr_failure").isJsonNull(),"JFR did not fail");
            check(metric(root,"expansion","render","VERTICES").get("count").getAsLong()==1234,"counter amount preserved");
            check(metric(root,"stable_transition","render","MESH_BUILD").get("count").getAsLong()==1,"mesh timer isolated in transition");
            check(metric(root,"stable","server","MIRROR_CHANGES").get("count").getAsLong()==7,"thread and stable phase separated");
            var frame=metric(root,"expansion","render","CLIENT_FRAME");
            check(frame.get("total_ms").getAsDouble()>0&&frame.get("cpu_total_ms").getAsDouble()>0,"frame includes CPU and wall time");
            check(root.getAsJsonObject("vertex_consumers").has(OpenDomainPerformanceRegression.class.getName()),"actual consumer implementation recorded");
            Set<String> phases=new HashSet<>();
            try(var paths=Files.list(directory)){
                Path jfr=paths.filter(p->p.toString().endsWith(".jfr")).findFirst().orElseThrow();
                for(var event:RecordingFile.readAllEvents(jfr))if(event.getEventType().getName().equals("innerdimension.OpenDomainPhase"))phases.add(event.getString("phase"));
            }
            check(phases.containsAll(Set.of("expansion","stable","finished")),"real JFR phase markers readable");

            // A canceled recorder's old deadline must never stop a new recording for the same owner.
            OpenDomainPerformance.start(directory,owner,Duration.ofSeconds(10));
            while(System.nanoTime()-firstStart<6_400_000_000L)LockSupport.parkNanos(20_000_000L);
            check(OpenDomainPerformance.begin()!=0,"old deadline cannot finish replacement recording");
            OpenDomainPerformance.finish(owner);
            await(()->files(directory,"-metrics.json")==2,"replacement recording saved");
            OpenDomainPerformance.start(directory,owner,Duration.ofMillis(350));
            await(()->files(directory,"-metrics.json")==3,"automatic timeout saves report");
            check(OpenDomainPerformance.begin()==0,"timeout disables timers");
            OpenDomainPerformance.count(OpenDomainPerformance.Metric.VERTICES,10);
            check(files(directory,"-metrics.json")==3,"disabled counters create no new recording");
        }finally{
            Thread.currentThread().setName(originalName);OpenDomainPerformance.shutdown();
        }
        System.out.println("OpenDomainPerformanceRegression passed: "+checks+" checks; outputs="+directory);
    }
}
