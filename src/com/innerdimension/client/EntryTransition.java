package com.innerdimension.client;

import com.innerdimension.network.EntryTransitionPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.class_310;
import net.minecraft.class_638;

/** Conceal the required animation reset, without changing NoN's state lifecycle. */
public final class EntryTransition {
    private static class_638 source;
    private static int remaining;
    private static long closingStarted, openingStarted;
    private static boolean forcedClosed;
    private static boolean complete;
    private static boolean rendered;
    private static int impact;
    private EntryTransition() {}
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(EntryTransitionPayload.ID, (payload, context) -> {
            if (payload.complete() && remaining <= 0) return;
            if (remaining <= 0) {
                source = context.client().field_1687;
                closingStarted = System.nanoTime() + (payload.impact() == 0 ? 80_000_000L : 180_000_000L);
                openingStarted = 0;
                rendered = false;
                forcedClosed = false;
                complete = false;
                impact = payload.impact();
                if(impact==3)EnlightenmentOverlay.prepareRitual();
            }
            // Match the server's 30-second chunk retry window; explicit completion
            // also releases the mask on a rejected transfer.
            if(payload.closed()&&impact==3&&payload.impact()==0){EnlightenmentOverlay.finishRitual();closingStarted=System.nanoTime();openingStarted=0;complete=false;forcedClosed=false;impact=0;}
            remaining = payload.closed() ? 640 : impact==3?640:40;
            forcedClosed |= payload.closed();
            complete |= payload.complete();
            org.slf4j.LoggerFactory.getLogger("inner_dimension.entry_transition").info("[InnerEntryMask] RECEIVED");
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> remaining = 0));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (remaining <= 0) return;
            if (client.field_1687 != source) {
                forcedClosed = true;
                complete = true;
            }
            openWhenReady();
            // Also reopen smoothly if entry is cancelled or blocked.
            if (!complete && --remaining <= 10 && openingStarted == 0) openingStarted = System.nanoTime();
            if (impact!=3 && openingStarted != 0 && System.nanoTime() - openingStarted >= openingDuration()) {
                remaining = 0; source = null;
            }
        });
    }
    /** Called after GUI extraction, before GUI submission, independent of HUD visibility. */
    public static void render(net.minecraft.class_332 graphics) {
        if(remaining<=0)EnlightenmentOverlay.renderPreview(graphics);
        if (remaining <= 0 || class_310.method_1551().field_1687 == null) return;
        openWhenReady();
        long now = System.nanoTime();
        double aperture = openingStarted != 0 ? smooth((now - openingStarted) / (double)openingDuration())
                : forcedClosed ? 0 : 1 - smooth((now - closingStarted) / 120_000_000.0);
        graphics.method_71048();
        graphics.method_51448().pushMatrix();
        graphics.method_51448().identity();
        if(impact==3&&openingStarted!=0)EnlightenmentOverlay.renderRitual(graphics,openingStarted);
        else EnlightenmentOverlay.renderClosing(graphics);
        drawEyelids(graphics,aperture);
        graphics.method_51448().popMatrix();
        if (!rendered) {
            rendered = true;
            org.slf4j.LoggerFactory.getLogger("inner_dimension.entry_transition").info("[InnerEntryMask] DRAWN");
        }
    }
    public static void drawEyelids(net.minecraft.class_332 graphics,double aperture){
        graphics.method_71048();
        graphics.method_51448().pushMatrix();graphics.method_51448().identity();
        int width=graphics.method_51421(),height=graphics.method_51443();
        if (aperture <= 0) graphics.method_25294(0, 0, width, height, 0xff000000);
        else if (aperture < 1) {
            // A gently curved slit, with feathered eyelid edges. At full opening
            // curvature disappears so the image reaches the screen edges smoothly.
            int feather = Math.max(2, height / 100);
            for (int x = 0; x < width; x += 4) {
                double side = (x + 2.0) * 2 / width - 1;
                double half = height * .5 * aperture * (1 - .28 * (1 - aperture) * side * side);
                int top = (int) Math.round(height * .5 - half);
                int bottom = (int) Math.round(height * .5 + half);
                int right = Math.min(width, x + 4);
                graphics.method_25294(x, 0, right, top, 0xff000000);
                graphics.method_25294(x, bottom, right, height, 0xff000000);
                for (int f = 0; f < feather && f < bottom - top; f++) {
                    int alpha = (feather - f) * 180 / feather;
                    graphics.method_25294(x, top + f, right, Math.min(bottom, top + f + 1), alpha << 24);
                    graphics.method_25294(x, Math.max(top, bottom - f - 1), right, bottom - f, alpha << 24);
                }
            }
        }
        graphics.method_51448().popMatrix();
    }
    private static long openingDuration(){return impact==3?4_000_000_000L:240_000_000L;}
    public static boolean ritualActive(){return remaining>0&&impact==3;}
    public static double ritualShake(){return remaining>0&&impact==3&&openingStarted!=0?Math.sin(System.nanoTime()/13_000_000.0)*2.5:0;}
    private static double smooth(double value) {
        double t = Math.max(0, Math.min(1, value));
        return t * t * (3 - 2 * t);
    }
    /** Push through closing, hold while shut, then return quickly during opening. */
    public static double cameraImpulse(int direction) {
        if (remaining <= 0 || impact != direction) return 0;
        long now = System.nanoTime();
        if (openingStarted != 0) return 1 - smooth((now - openingStarted) / 140_000_000.0);
        // Eyelids finish in 120 ms; the impulse peaks 40 ms later, under full cover.
        // Start 140 ms before eyelid motion, keeping the fully-shut peak time unchanged.
        return smooth((now - closingStarted + 140_000_000L) / 300_000_000.0);
    }
    private static void openWhenReady() {
        if (!complete || openingStarted != 0) return;
        var client = class_310.method_1551();
        if (client.field_1687 == null || client.field_1724 == null
                || client.field_1755 instanceof net.minecraft.class_3928) return;
        var handler = client.method_1562();
        if (handler == null) return;
        var tracker = ((com.innerdimension.mixin.EntryLoadingAccessor) handler).inner$loadingTracker();
        if (tracker != null && !tracker.method_72902()) return;
        openingStarted = System.nanoTime();
        if(impact==3)InnerSoundClient.personal("enlightenment",.8f);
    }
}
