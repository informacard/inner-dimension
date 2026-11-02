package com.innerdimension.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_10042;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * Compile-time stand-in for GeckoLib's render state.
 *
 * <p>GeckoLib injects {@link GeoRenderState} into the vanilla render state classes with a
 * Fabric class-tweaker, which is applied by Loom but not by this workspace's plain javac
 * toolchain. Without it the generic bound of {@code GeoEntityRenderer} is unsatisfiable,
 * so we declare the type ourselves. At runtime GeckoLib still creates plain vanilla
 * {@code class_10042} / {@code class_10017} states (see {@code GeoEntityRenderer#createRenderState}),
 * and every GeckoLib cast is to those vanilla types, so this class is never instantiated
 * and only serves to satisfy the compiler.</p>
 */
public class DildoRenderState extends class_10042 implements GeoRenderState {
    private final Map<DataTicket<?>, Object> geckolibData = new HashMap<>();

    @Override
    public Map<DataTicket<?>, Object> getDataMap() {
        return this.geckolibData;
    }
}
