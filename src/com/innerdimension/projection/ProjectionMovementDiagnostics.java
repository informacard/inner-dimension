package com.innerdimension.projection;

import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Opt-in, UUID-scoped and time-bounded; disabled during ordinary play. */
public final class ProjectionMovementDiagnostics {
    private static final Logger LOGGER = LoggerFactory.getLogger("inner_dimension/movement");
    private static final String PLAYER = Boolean.getBoolean("inner_dimension.movement.enabled")
            ? System.getProperty("inner_dimension.movement.player", "") : "";
    private static final long DURATION = Math.max(1, Math.min(300,
            Long.getLong("inner_dimension.movement.seconds", 60L))) * 1_000_000_000L;
    private static final ThreadLocal<Session> SESSION = ThreadLocal.withInitial(Session::new);
    private static final class Session {
        long started;
        int queries;
    }
    private ProjectionMovementDiagnostics() {}
    private static boolean enabled(class_1297 entity) {
        if (entity == null || PLAYER.isEmpty() || !PLAYER.equals(entity.method_5667().toString())) return false;
        Session session = SESSION.get();
        if (session.started == 0) session.started = System.nanoTime();
        return System.nanoTime() - session.started < DURATION;
    }
    public static void begin(class_1297 entity, class_243 requested) {
        if (!enabled(entity)) return;
        SESSION.get().queries = 0;
        LOGGER.info("projection-move begin world={} player={} y={} velocity={} requested={} ground={} horizontal={} vertical={} fall={} step={}",
                entity.method_73183().method_27983(), PLAYER, entity.method_23318(), entity.method_18798(),
                requested, entity.method_24828(), entity.field_5976, entity.field_5992, entity.field_6017, entity.method_49476());
    }
    public static void query(class_1297 entity, class_238 box, int shapes) {
        if (!enabled(entity)) return;
        LOGGER.info("projection-move query={} box={} projected={}", ++SESSION.get().queries, box, shapes);
    }
    public static void end(class_1297 entity, class_243 requested, class_243 actual) {
        if (!enabled(entity)) return;
        LOGGER.info("projection-move solve requested={} actual={} queries={} extraUp={}", requested, actual,
                SESSION.get().queries, actual.field_1351 > Math.max(0, requested.field_1351) + 1.0E-7);
    }
    public static void after(class_1297 entity) {
        if (!enabled(entity)) return;
        LOGGER.info("projection-move after y={} velocity={} ground={} horizontal={} vertical={} fall={}",
                entity.method_23318(), entity.method_18798(), entity.method_24828(),
                entity.field_5976, entity.field_5992, entity.field_6017);
    }

    public static void solverQuery(class_1297 entity, net.minecraft.class_1937 world,
            class_238 body, class_243 movement, int shapes) {
        if (!enabled(entity)) return;
        LOGGER.info("projection-move solver-query world={} body={} requested={} projected={}",
                world.method_27983(), body, movement, shapes);
    }

    public static void solverResult(class_1297 entity, class_243 requested, class_243 actual) {
        if (!enabled(entity)) return;
        LOGGER.info("projection-move solver-solve requested={} actual={}", requested, actual);
    }
}
