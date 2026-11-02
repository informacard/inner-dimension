package com.innerdimension.client;

import com.innerdimension.dimension.DynamicDimensionManager;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.class_310;
import net.minecraft.class_638;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Temporary, bounded measurements of the first two inner-world round trips. */
public final class TeleportDiagnostics {
    private static final Logger LOG = LoggerFactory.getLogger("inner_dimension.teleport.diagnostics");
    private static final String TAG = "[InnerTeleportDiag]";
    private static final long WINDOW = 8_000_000_000L;
    private static final Map<String, long[]> SPANS = new LinkedHashMap<>();
    private static boolean armed, recordingStarted;
    private static int sequence, slowFrames, printedFrames;
    private static long until, started, previousFrameEnd, maxFrame, gcStart;
    private static Recording recording;
    private TeleportDiagnostics() {}

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            finish();
            armed = true;
            sequence = 0;
            previousFrameEnd = 0;
            if (Boolean.getBoolean("inner_dimension.teleportJfr")) startRecording();
            LOG.info("{} ARMED first_four_inner_world_switches window_ms=8000", TAG);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> {
            finish();
            armed = false;
        }));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (until != 0 && System.nanoTime() > until) finish();
        });
    }

    private static void startRecording() {
        if (recordingStarted) return;
        recordingStarted = true;
        try {
            Path folder = Path.of("logs").toAbsolutePath();
            Files.createDirectories(folder);
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path output = folder.resolve("inner-teleport-" + time + ".jfr");
            recording = new Recording(Configuration.getConfiguration("profile"));
            recording.setName("Inner Dimension first teleport");
            recording.setDuration(Duration.ofSeconds(120));
            recording.setMaxSize(64L * 1024 * 1024);
            recording.setDestination(output);
            recording.setDumpOnExit(true);
            recording.start();
            LOG.info("{} JFR_STARTED duration_seconds=120 max_mb=64 file={}", TAG, output);
        } catch (Exception failure) {
            if (recording != null) recording.close();
            recording = null;
            LOG.warn("{} JFR_UNAVAILABLE {} (timing logs remain active)", TAG, failure.toString());
        }
    }

    public static void worldChange(class_638 from, class_638 to) {
        if (!armed || sequence >= 4 || from == null || to == null
                || from.method_27983().equals(to.method_27983())
                || !(DynamicDimensionManager.isInnerWorld(from.method_27983())
                || DynamicDimensionManager.isInnerWorld(to.method_27983()))) return;
        finish();
        sequence++;
        started = System.nanoTime();
        until = started + WINDOW;
        slowFrames = printedFrames = 0;
        maxFrame = 0;
        gcStart = gcMillis();
        LOG.info("{} SWITCH seq={} from={} to={} wall_ms={}", TAG, sequence,
                from.method_27983().method_29177(), to.method_27983().method_29177(), System.currentTimeMillis());
    }

    public static long begin() { return until != 0 && System.nanoTime() <= until ? System.nanoTime() : 0; }

    public static void span(String name, long begin) {
        if (begin == 0) return;
        long elapsed = System.nanoTime() - begin;
        long[] stat = SPANS.computeIfAbsent(name, key -> new long[3]);
        stat[0]++;
        stat[1] += elapsed;
        stat[2] = Math.max(stat[2], elapsed);
        if (elapsed >= 10_000_000L && stat[0] <= 30)
            LOG.info("{} SPAN seq={} name={} elapsed_ms={} since_switch_ms={}",
                    TAG, sequence, name, millis(elapsed), millis(System.nanoTime() - started));
    }

    public static void frame(long begin) {
        long end = System.nanoTime();
        if (until != 0) {
            long duration = end - begin;
            long gap = previousFrameEnd == 0 ? 0 : begin - previousFrameEnd;
            maxFrame = Math.max(maxFrame, duration);
            if (duration >= 50_000_000L || gap >= 100_000_000L) {
                slowFrames++;
                if (printedFrames++ < 20) {
                    var client = class_310.method_1551();
                    LOG.info("{} LONG_FRAME seq={} frame_ms={} between_frames_ms={} since_switch_ms={} screen={}",
                            TAG, sequence, millis(duration), millis(gap), millis(end - started),
                            client.field_1755 == null ? "none" : client.field_1755.getClass().getSimpleName());
                }
            }
        }
        previousFrameEnd = end;
    }

    private static void finish() {
        if (until == 0) return;
        LOG.info("{} SUMMARY seq={} observed_ms={} slow_frames={} max_frame_ms={} gc_collection_counter_ms={}",
                TAG, sequence, millis(System.nanoTime() - started), slowFrames, millis(maxFrame), gcMillis() - gcStart);
        SPANS.forEach((name, stat) -> LOG.info("{} TOTAL seq={} name={} calls={} total_ms={} max_ms={}",
                TAG, sequence, name, stat[0], millis(stat[1]), millis(stat[2])));
        SPANS.clear();
        until = 0;
    }

    private static long gcMillis() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
                .mapToLong(bean -> Math.max(0, bean.getCollectionTime())).sum();
    }
    private static double millis(long nanos) { return Math.round(nanos / 100_000.0) / 10.0; }
}
