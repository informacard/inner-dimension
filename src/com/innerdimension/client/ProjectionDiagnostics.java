package com.innerdimension.client;

import com.innerdimension.config.InnerConfig;
import com.innerdimension.projection.RoomProjectionEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.class_310;
import org.slf4j.LoggerFactory;
import java.util.IdentityHashMap;
import java.util.Map;

/** Observes entity arrival independently of entity ticks and visibility decisions. */
public final class ProjectionDiagnostics {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("inner_dimension.projection.debug");
    private static final class Observation {
        long extracts, submits;
        long checks, accepted;
        long previousTicks;
        String visibility = "not-checked";
        String mesh = "not-extracted";
    }
    private static final Map<RoomProjectionEntity, Observation> ENTITIES = new IdentityHashMap<>();
    private static int ticks;
    private static final class CullingAccess {
        static final Class<?> TYPE;
        static {
            Class<?> type = null;
            try { type = Class.forName("dev.tr7zw.entityculling.versionless.access.Cullable"); }
            catch (ClassNotFoundException ignored) { }
            TYPE = type;
        }
        static String read(RoomProjectionEntity entity) {
            if (TYPE == null || !TYPE.isInstance(entity)) return "unavailable";
            try {
                return "culled=" + TYPE.getMethod("isCulled").invoke(entity)
                        + ",outOfCamera=" + TYPE.getMethod("isOutOfCamera").invoke(entity)
                        + ",forcedVisible=" + TYPE.getMethod("isForcedVisible").invoke(entity);
            } catch (ReflectiveOperationException failure) { return "read-error:" + failure.getClass().getSimpleName(); }
        }
    }
    public static void visibility(RoomProjectionEntity entity, boolean accepted, boolean distance, boolean frustum) {
        if (!InnerConfig.get().projectionDebug) return;
        Observation observation = ENTITIES.get(entity);
        if (observation == null) return;
        observation.checks++;
        if (accepted) observation.accepted++;
        observation.visibility = "accepted=" + accepted + ",distance=" + distance + ",boundsInFrustum=" + frustum;
    }
    public static void register() {
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof RoomProjectionEntity projection) {
                ENTITIES.put(projection, new Observation());
                event("CLIENT_LOAD", projection);
            }
        });
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof RoomProjectionEntity projection) {
                event("CLIENT_UNLOAD", projection);
                ENTITIES.remove(projection);
            }
        });
    }
    public static void event(String event, RoomProjectionEntity entity) {
        if (!InnerConfig.get().projectionDebug) return;
        var structure = entity.getContraption();
        LOG.info("[ProjectionDebug] {} uuid={} side={} shell={} phase={} elapsed={} position={} bounds={} structure={} blocks={}",
                event, entity.method_5667(), "client",
                entity.isShell(), entity.phase(), entity.elapsed(0), entity.method_73189(), entity.method_5829(),
                structure != null, structure == null ? -1 : structure.getBlocks().size());
    }
    public static void extracted(RoomProjectionEntity entity, String mesh) {
        if (!InnerConfig.get().projectionDebug) return;
        Observation observation = ENTITIES.get(entity);
        if (observation != null) { if (mesh.equals("extract-start")) observation.extracts++; observation.mesh = mesh; }
    }
    public static void submitted(RoomProjectionEntity entity) {
        if (!InnerConfig.get().projectionDebug) return;
        Observation observation = ENTITIES.get(entity);
        if (observation != null) observation.submits++;
    }
    public static void tick(class_310 client) {
        ENTITIES.keySet().removeIf(entity -> entity.method_7325() || entity.method_73183() != client.field_1687);
        if (!InnerConfig.get().projectionDebug || ++ticks % 20 != 0) return;
        ENTITIES.forEach((entity, observation) -> {
            event("CLIENT_SAMPLE", entity);
            double distance = client.field_1724 == null ? -1 : entity.method_73189().method_1022(client.field_1724.method_73189());
            LOG.info("[ProjectionDebug] CLIENT_RENDER uuid={} extracts={} submits={} mesh={} playerDistance={}",
                    entity.method_5667(), observation.extracts, observation.submits, observation.mesh, distance);
            var origin = entity.method_24515();
            var box = entity.method_5829();
            var center = new net.minecraft.class_2338((int)Math.floor((box.field_1323 + box.field_1320) / 2),
                    (int)Math.floor((box.field_1322 + box.field_1325) / 2),
                    (int)Math.floor((box.field_1321 + box.field_1324) / 2));
            boolean originOutsideHeight = client.field_1687.method_31601(origin.method_10264());
            boolean centerOutsideHeight = client.field_1687.method_31601(center.method_10264());
            boolean originSection = client.field_1769.method_40050(origin);
            boolean centerSection = client.field_1769.method_40050(center);
            var renderPosition = ProjectionRenderPosition.sectionPosition(entity, origin);
            boolean renderSection = client.field_1769.method_40050(renderPosition);
            LOG.info("[ProjectionDebug] CLIENT_SECTION uuid={} lookup={} sectionVisible={} effectiveGate={}",
                    entity.method_5667(), renderPosition, renderSection,
                    client.field_1687.method_31601(renderPosition.method_10264()) || renderSection);
            LOG.info("[ProjectionDebug] CLIENT_GATES uuid={} ready={} aliveOrStale={} staleTicks={} checks={} accepted={} lastCheck={} fullTicks={} culling={} origin={} originOutsideHeight={} originSectionVisible={} originGate={} center={} centerOutsideHeight={} centerSectionVisible={}",
                    entity.method_5667(), entity.isReadyForRender(), entity.isAliveOrStale(), entity.staleTicks,
                    observation.checks, observation.accepted, observation.visibility,
                    entity.diagnosticContraptionTicks() - observation.previousTicks, CullingAccess.read(entity),
                    origin, originOutsideHeight, originSection, originOutsideHeight || originSection,
                    center, centerOutsideHeight, centerSection);
            observation.previousTicks = entity.diagnosticContraptionTicks();
            observation.checks = observation.accepted = 0;
            observation.extracts = observation.submits = 0;
        });
    }
}
