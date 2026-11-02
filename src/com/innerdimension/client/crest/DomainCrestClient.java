package com.innerdimension.client.crest;

import com.innerdimension.InnerDimension;
import com.innerdimension.client.DomainCrestFeature;
import com.innerdimension.mixin.NonDestroyedSkinClientAccessor;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3300;
import net.minecraft.class_746;
import org.slf4j.LoggerFactory;

/** Client visual state and invalidation of NoN skin textures and world-space crests. */
public final class DomainCrestClient implements SimpleSynchronousResourceReloadListener {
    private record State(boolean active,int revision,int age,int burnout) {}
    private static final Map<UUID, State> STATES = new HashMap<>();
    private static BufferedImage overlay;
    private static int generation;

    public static boolean active(UUID id) {
        State state = STATES.get(id);
        return state != null && state.active();
    }

    public static int revision(UUID id) {
        State state = STATES.get(id);
        var client=class_310.method_1551();var projection=client.field_1687==null?null:com.innerdimension.projection.ProjectionIndex.active(client.field_1687,id);
        return (state == null ? 0 : state.revision() + generation * 1000 + (state.burnout()>0 ? 500 : 0))+(projection==null?0:10000+projection.expansionSlot()*100000);
    }

    public static boolean burnedOut(UUID id) { State s=STATES.get(id); return s!=null && s.burnout()>0; }
    public static float strength(UUID id,float partial) {
        State s=STATES.get(id);
        var client=class_310.method_1551();var projection=client.field_1687==null?null:com.innerdimension.projection.ProjectionIndex.active(client.field_1687,id);
        if(projection!=null&&projection.open())return s==null||s.age()<0?0:(float)Math.max(0,1-(s.age()+partial)/projection.maximumTicks());
        return s==null || s.age()<0 ? 0 : com.innerdimension.projection.DomainLifetime.strength(s.age()+partial);
    }
    public static void accept(UUID id,boolean active,int revision) { accept(id,active,revision,-1,0); }
    public static void accept(UUID id,boolean active,int revision,int age,int burnout) {
        // Inactive also represents departure: do not retain offline player UUIDs.
        State old = active ? STATES.put(id, new State(true,revision,age,burnout)) : STATES.remove(id);
        if (old == null || old.active() != active || old.revision() != revision || (old.burnout()>0)!=(burnout>0)) {
            CrestImageComposer.clear();
            invalidatePlayer(id);
        }
    }

    private static void invalidatePlayer(UUID id) {
        DomainCrestFeature.invalidate(id);
        NonDestroyedSkinClientAccessor.inner$clearCachedTextures(id);
    }

    public static byte[] derive(UUID id, byte[] original) {
        return active(id) ? CrestImageComposer.compose(id, original, skinDesignFor(id),
                "standard_hd", 3, revision(id)) : original;
    }

    private static BufferedImage skinDesignFor(UUID id) {
        BufferedImage design=designFor(id);
        if(design==null || !burnedOut(id)) return design;
        BufferedImage faded=new BufferedImage(design.getWidth(),design.getHeight(),BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=faded.createGraphics();
        try {
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC,.03f));
            int w=Math.max(1,(int)Math.round(design.getWidth()*.9)),h=Math.max(1,(int)Math.round(design.getHeight()*.9));
            g.drawImage(design,(design.getWidth()-w)/2,(design.getHeight()-h)/2,w,h,null);
        } finally { g.dispose(); }
        return faded;
    }
    public static BufferedImage currentDesign() {
        return CrestDesignStore.current(copy(overlay));
    }

    public static void applyLocalDesign(BufferedImage design, int resolution) {
        BufferedImage previous = currentDesign();
        boolean changed = previous.getWidth() != design.getWidth() || previous.getHeight() != design.getHeight();
        if (!changed) for (int y=0; y<design.getHeight() && !changed; y++) for (int x=0; x<design.getWidth(); x++)
            if (previous.getRGB(x,y) != design.getRGB(x,y)) { changed=true; break; }
        CrestDesignStore.setCurrent(design, resolution);
        if (changed && net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.canSend(com.innerdimension.network.CrestChangedPayload.ID))
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(com.innerdimension.network.CrestChangedPayload.INSTANCE);
        generation++;
        CrestImageComposer.clear();
        UUID id = localPlayer();
        if (id != null) invalidatePlayer(id);
    }

    public static void clear() {
        class_310 client = class_310.method_1551();
        if (!client.method_18854()) {
            client.execute(DomainCrestClient::clear);
            return;
        }
        for (UUID id : List.copyOf(STATES.keySet())) invalidatePlayer(id);
        DomainCrestFeature.clearTextures();
        STATES.clear();
        CrestImageComposer.clear();
    }

    @Override
    public class_2960 getFabricId() {
        return InnerDimension.id("domain_crest");
    }

    @Override
    public void method_14491(class_3300 manager) {
        try (InputStream in = manager.open(InnerDimension.id("textures/crest/default_lewd_crest.png"))) {
            BufferedImage atlas = ImageIO.read(in);
            if (atlas == null) throw new IOException("Unsupported crest image");
            CrestDesignStore.load(atlas);
            overlay = CrestImageComposer.normalizeDesign(atlas, 256);
        } catch (IOException | RuntimeException failure) {
            overlay = null;
            LoggerFactory.getLogger("inner_dimension").warn("Unable to reload domain crest", failure);
        }
        // Failed reloads must invalidate previous textures too.
        generation++;
        CrestImageComposer.clear();
        DomainCrestFeature.clearTextures();
        for (UUID id : List.copyOf(STATES.keySet())) {
            NonDestroyedSkinClientAccessor.inner$clearCachedTextures(id);
        }
    }

    public static BufferedImage designFor(UUID id) {
        UUID local = localPlayer();
        if(local!=null&&local.equals(id)){var client=class_310.method_1551();var p=client.field_1687==null?null:com.innerdimension.projection.ProjectionIndex.active(client.field_1687,id);return p!=null?CrestDesignStore.slot(p.expansionSlot()):currentDesign();}return copy(overlay);
    }

    private static UUID localPlayer() {
        class_746 player = class_310.method_1551().field_1724;
        return player == null ? null : player.method_5667();
    }

    private static BufferedImage copy(BufferedImage image) {
        if (image == null) return null;
        BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = out.createGraphics();
        try {
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return out;
    }
}
