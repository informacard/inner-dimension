package com.innerdimension.projection;

import com.google.gson.GsonBuilder;
import jdk.jfr.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.lang.management.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.slf4j.LoggerFactory;

/** Opt-in, one-shot observation. No clocks in vertex loops and no gameplay changes. */
public final class OpenDomainPerformance {
    public enum Metric {
        CLIENT_FRAME, SCENE_RENDER, SERVER_TICK, PAUSED_FRAMES, MENU_FRAMES, FPS_LIMIT_SUM,
        RENDER_EXTRACT, MESH_BUILD, BLOCK_ENTITY_RENDER, LIGHT_PREPARE, VERTEX_WRITE, MIRROR_TICK, ABILITY_TICK, CARRIER_TICK,
        COLLISION_QUERY, FLUID_QUERY, PATH_QUERY, SHAPE_BUILD, APPLY_UPDATE,
        VERTICES, LIGHT_QUERIES, SPATIAL_CANDIDATES, MIRROR_CHANGES, UPDATE_ENTRIES, FULL_SYNC_ENTRIES
    }
    @Name("innerdimension.OpenDomainPhase") @Label("Open domain phase")
    @Category("Inner Dimension") @StackTrace(false)
    public static final class PhaseEvent extends Event { public String phase; }
    private static final Metric[] METRICS=Metric.values();
    private static final class CpuHolder {static final ThreadMXBean CPU=ManagementFactory.getThreadMXBean();}
    private static volatile Probe active;
    private OpenDomainPerformance() { }

    public static void startIfArmed(UUID owner) {
        // JFR startup can redefine classes and invalidate compiled code across the JVM.
        // Ordinary play never starts it; profiling requires both explicit opt-in and a marker.
        if(!Boolean.getBoolean("inner_dimension.performance.record"))return;
        Path directory=FabricLoader.getInstance().getConfigDir().resolve("inner_dimension/performance");
        Path flag=directory.resolve("record-next-open.flag");
        if(active!=null||!Files.isRegularFile(flag))return;
        try {
            if(!Files.deleteIfExists(flag))return;
            start(directory,owner,Duration.ofSeconds(22));
        } catch(Exception failure) {LoggerFactory.getLogger("inner_dimension.performance").warn("Cannot arm open-domain observation",failure);}
    }
    static synchronized void start(Path directory,UUID owner,Duration duration) throws Exception {
        if(active!=null)return;
        Files.createDirectories(directory);
        Probe probe=new Probe(directory,owner);active=probe;
        probe.executor.execute(()->{
            try {
                Recording recording=new Recording(Configuration.getConfiguration("profile"));
                probe.recording=recording;
                recording.enable(PhaseEvent.class);
                recording.setName("Inner Dimension open-domain observation");
                recording.setDestination(probe.file);recording.setDumpOnExit(true);
                recording.setMaxSize(64L*1024*1024);
                recording.start();phase(probe.stableNanos==0?"expansion":"stable");
                LoggerFactory.getLogger("inner_dimension.performance").info("Open-domain observation started: {}",probe.file);
            } catch(Exception failure) {
                probe.failure=failure.toString();
                if(probe.recording!=null)try{probe.recording.close();}catch(Exception ignored){}
                LoggerFactory.getLogger("inner_dimension.performance").warn("JFR unavailable; keeping phase timing counters",failure);
            }
        });
        probe.deadline=probe.executor.schedule(()->finish(probe),duration.toMillis(),TimeUnit.MILLISECONDS);
    }
    public static synchronized void stable(UUID owner) {
        Probe p=active;if(p==null||!Objects.equals(p.owner,owner))return;
        if(p.stableNanos==0){p.stableNanos=System.nanoTime();phase("stable");}
    }
    public static synchronized void finish(UUID owner) {
        Probe p=active;if(p!=null&&Objects.equals(p.owner,owner))finish(p);
    }
    private static synchronized void finish(Probe p) {
        if(active!=p||!p.finishing.compareAndSet(false,true))return;
        p.endedNanos=System.nanoTime();
        phase("finished");active=null;
        if(p.deadline!=null)p.deadline.cancel(false);
        p.executor.execute(()->{
            try {
                // stop() writes the configured destination. Dumping to that same Windows file
                // again can fail while JFR still holds its destination handle.
                if(p.recording!=null&&p.recording.getState()==RecordingState.RUNNING)p.recording.stop();
            } catch(Exception failure) {p.failure=failure.toString();LoggerFactory.getLogger("inner_dimension.performance").warn("Cannot save open-domain JFR",failure);}
            finally {
                if(p.recording!=null)try{p.recording.close();}catch(Exception ignored){}
                try{p.write();LoggerFactory.getLogger("inner_dimension.performance").info("Open-domain observation saved: {}",p.file);}
                catch(Exception failure){LoggerFactory.getLogger("inner_dimension.performance").warn("Cannot save open-domain timing counters",failure);}
                p.executor.shutdown();
            }
        });
    }
    public static long begin() {return active==null?0:System.nanoTime();}
    public static void end(Metric metric,long start) {
        if(start==0)return;Probe p=active;if(p==null)return;
        p.add(p.slot(metric),System.nanoTime()-start,0);
    }
    /** Only used for frame/tick envelopes, so thread CPU sampling stays out of block/vertex loops. */
    public static Span span(Metric metric) {
        Probe p=active;if(p==null)return null;
        return new Span(p,p.slot(metric),System.nanoTime(),cpuTime());
    }
    public static void end(Span span) {
        if(span==null||active!=span.probe)return;
        long cpu=span.cpuStart<0?0:Math.max(0,cpuTime()-span.cpuStart);
        span.probe.add(span.slot,System.nanoTime()-span.wallStart,cpu);
    }
    private static long cpuTime(){var cpu=CpuHolder.CPU;return cpu.isCurrentThreadCpuTimeSupported()&&cpu.isThreadCpuTimeEnabled()?cpu.getCurrentThreadCpuTime():-1;}
    public static final class Span {
        private final Probe probe;private final int slot;private final long wallStart,cpuStart;
        private Span(Probe probe,int slot,long wallStart,long cpuStart){this.probe=probe;this.slot=slot;this.wallStart=wallStart;this.cpuStart=cpuStart;}
    }
    public static void shutdown(){Probe p=active;if(p!=null)finish(p);}
    public static void count(Metric metric,long amount) {
        Probe p=active;if(p!=null)p.calls[p.slot(metric)].add(amount);
    }
    public static void consumer(Class<?> type) {
        Probe p=active;if(p!=null)p.consumers.computeIfAbsent(type.getName(),ignored->new LongAdder()).increment();
    }
    private static void phase(String phase) {PhaseEvent event=new PhaseEvent();event.phase=phase;event.commit();}
    private static final class Probe {
        final UUID owner;final Path file;final long startedNanos=System.nanoTime();
        volatile long stableNanos,endedNanos;volatile Recording recording;volatile String failure;
        volatile ScheduledFuture<?> deadline;
        final AtomicBoolean finishing=new AtomicBoolean();
        final LongAdder[] calls=new LongAdder[3*3*METRICS.length],nanos=new LongAdder[calls.length],cpuNanos=new LongAdder[calls.length];
        final LongAccumulator[] maximum=new LongAccumulator[calls.length];
        final ConcurrentMap<String,LongAdder> consumers=new ConcurrentHashMap<>();
        final ScheduledExecutorService executor=Executors.newSingleThreadScheduledExecutor(task->{Thread t=new Thread(task,"Inner Dimension performance recorder");t.setDaemon(true);return t;});
        Probe(Path directory,UUID owner) {
            this.owner=owner;
            String stamp=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
            file=directory.resolve("open-domain-"+stamp+".jfr");
            for(int i=0;i<calls.length;i++){calls[i]=new LongAdder();nanos[i]=new LongAdder();cpuNanos[i]=new LongAdder();maximum[i]=new LongAccumulator(Long::max,0);}
        }
        void add(int slot,long elapsed,long cpu){calls[slot].increment();nanos[slot].add(elapsed);cpuNanos[slot].add(cpu);maximum[slot].accumulate(elapsed);}
        int slot(Metric metric) {
            long stable=stableNanos;int phase=stable==0?0:System.nanoTime()-stable<1_000_000_000L?1:2;
            String name=Thread.currentThread().getName();int thread=name.equals("Render thread")?0:name.equals("Server thread")?1:2;
            return (phase*3+thread)*METRICS.length+metric.ordinal();
        }
        void write() throws Exception {
            Map<String,Object> root=new LinkedHashMap<>();root.put("elapsed_seconds",(endedNanos-startedNanos)/1e9);
            long expansion=stableNanos==0?endedNanos-startedNanos:stableNanos-startedNanos;
            long transition=stableNanos==0?0:Math.min(1_000_000_000L,endedNanos-stableNanos);
            long stable=stableNanos==0?0:Math.max(0,endedNanos-stableNanos-transition);
            root.put("phase_seconds",Map.of("expansion",expansion/1e9,"stable_transition",transition/1e9,"stable",stable/1e9));
            root.put("stable_seen",stableNanos!=0);root.put("jfr_failure",failure);
            root.put("owner",owner.toString());
            root.put("thread_cpu_time_available",CpuHolder.CPU.isCurrentThreadCpuTimeSupported()&&CpuHolder.CPU.isThreadCpuTimeEnabled());
            root.put("note","Timers include all projection activity in this JVM during the window. Totals are inclusive and nested; do not sum them as disjoint frame time. CLIENT_FRAME includes tick, display swap and FPS limiting. CPU timing is collected only for frame/render/server envelopes and excludes waits. stable excludes the first second after stabilization. Count metrics have zero timing.");
            List<Map<String,Object>> metrics=new ArrayList<>();String[] phases={"expansion","stable_transition","stable"},threads={"render","server","other"};
            for(int phase=0;phase<3;phase++)for(int thread=0;thread<3;thread++)for(Metric metric:METRICS){
                int slot=(phase*3+thread)*METRICS.length+metric.ordinal();long count=calls[slot].sum(),time=nanos[slot].sum();
                if(count==0)continue;Map<String,Object> row=new LinkedHashMap<>();row.put("phase",phases[phase]);row.put("thread",threads[thread]);row.put("metric",metric.name());row.put("count",count);row.put("total_ms",time/1e6);row.put("mean_ms",time/(1e6*count));row.put("max_ms",maximum[slot].get()/1e6);
                if(metric==Metric.CLIENT_FRAME||metric==Metric.SCENE_RENDER||metric==Metric.SERVER_TICK){row.put("cpu_total_ms",cpuNanos[slot].sum()/1e6);row.put("cpu_mean_ms",cpuNanos[slot].sum()/(1e6*count));}metrics.add(row);
            }
            root.put("metrics",metrics);Map<String,Long> implementations=new TreeMap<>();consumers.forEach((key,value)->implementations.put(key,value.sum()));root.put("vertex_consumers",implementations);
            Path json=file.resolveSibling(file.getFileName().toString().replace(".jfr","-metrics.json"));
            Files.writeString(json,new GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(root));
        }
    }
}
