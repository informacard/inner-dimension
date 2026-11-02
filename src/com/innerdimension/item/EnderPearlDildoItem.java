package com.innerdimension.item;

import com.innerdimension.binding.DollBindingManager;
import com.innerdimension.entity.EnderPearlDildoEntity;
import com.innerdimension.entity.ModEntities;
import java.util.function.Consumer;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_2338;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1792.class_1793;
import net.minecraft.class_1937;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3532;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3965;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * The dildo item: places the {@link EnderPearlDildoEntity} in front of the player, and
 * renders with the same GeckoLib model in hand / inventory / on the ground.
 */
public class EnderPearlDildoItem extends class_1792 implements GeoItem {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final boolean tethered;
    private final net.minecraft.class_1299<EnderPearlDildoEntity> entityType;
    private boolean syncedAnimatableRegistered;

    public EnderPearlDildoItem(class_1793 settings) {
        this(settings, false);
    }

    public EnderPearlDildoItem(class_1793 settings, boolean tethered) {
        super(settings);
        this.tethered = tethered;
        this.entityType = tethered
                ? ModEntities.TETHERED_ENDER_PEARL_DILDO
                : ModEntities.ENDER_PEARL_DILDO;
    }

    @Override
    public class_1269 method_7836(class_1937 world, class_1657 user, class_1268 hand) {
        class_3965 hit = class_1792.method_7872(world, user, class_242.field_1348);
        if (user.method_5715() && hit.method_17783() == class_240.field_1332
                && world.method_8320(hit.method_17777()).method_26204()
                == com.innerdimension.block.ModBlocks.PEARL_FACE) {
            if (user instanceof net.minecraft.class_3222 player)
                com.innerdimension.pearl.PearlSurfaceTransport.leaveFromSurface(player, world);
            // Consume this click on both sides even when exit resolution fails.
            return class_1269.field_5812;
        }
        if (hit.method_17783() != class_240.field_1332) {
            return class_1269.field_5811;
        }

        EnderPearlDildoEntity dildo = new EnderPearlDildoEntity(this.entityType, world);
        // Snap to the centre of the top face of the hit block instead of the exact point
        // the player aimed at, so the stick always sits neatly inside one cell.
        class_2338 hitPos = hit.method_17777();
        dildo.method_5814(hitPos.method_10263() + 0.5D,
                hitPos.method_10264() + 1.0D,
                hitPos.method_10260() + 0.5D);
        dildo.method_36456(class_3532.method_15393((float) Math.round(user.method_36454() / 90.0F) * 90.0F));

        if (!world.method_8587(dildo, dildo.method_5829())) {
            return class_1269.field_5814;
        }
        if (!world.method_8608()) {
            // Security spec section 3.2: the placer is written before the entity appears and before
            // any detection runs, so a rescan can never be the thing that supplies it.
            dildo.setPlacerUuid(user.method_5667());
            // A device placed by this build is not a legacy one: its first legal connection must
            // become the strong port immediately, not after a migration window.
            dildo.markMigrated();
            world.method_8649(dildo);
            world.method_8396(null, dildo.method_24515(), class_3417.field_15134, class_3419.field_15245, 1.0F, 0.8F);
            // A freshly placed stick detects the doll standing above it right away, so it joins
            // the owner's binding set without waiting for the next periodic sweep.
            DollBindingManager.onDildoPlaced(dildo);
            if (!user.method_68878()) {
                user.method_5998(hand).method_7934(1);
            }
        }
        return class_1269.field_5812;
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        if (!this.syncedAnimatableRegistered) {
            GeoItem.registerSyncedAnimatable(this);
            this.syncedAnimatableRegistered = true;
        }
        return this.geoCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<EnderPearlDildoItem> renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<>(new EnderPearlDildoItemModel(
                            tethered ? "item/roped_ender_pearl_dildo" : "item/ender_pearl_dildo"));
                }
                return this.renderer;
            }
        });
    }
}
