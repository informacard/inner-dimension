package com.innerdimension.mixin;

import com.innerdimension.client.EntryTransition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Offset the final animated camera each frame; never modify the player's pose or velocity. */
@Mixin(class_4184.class)
public abstract class EntryImpactCameraMixin {
    @Shadow protected abstract void method_19327(double x, double y, double z);
    @Shadow protected abstract void method_19324(float forward, float up, float sideways);
    @Shadow protected abstract void method_19325(float yaw, float pitch);

    @WrapMethod(method = "method_19321")
    private void inner$entryImpact(class_1937 world, class_1297 actor, boolean thirdPerson,
                                   boolean inverseView, float delta, Operation<Void> original) {
        original.call(world, actor, thirdPerson, inverseView, delta);
        if (actor != class_310.method_1551().field_1724) return;
        double up = EntryTransition.cameraImpulse(1);
        double forward = EntryTransition.cameraImpulse(2);
        double shake=EntryTransition.ritualShake();
        if (up == 0 && forward == 0 && shake==0 && !EntryTransition.ritualActive()) return;
        var camera = (class_4184) (Object) this;
        if (up != 0) {
            var position = camera.method_71156();
            method_19327(position.field_1352, position.field_1351 + .14 * up, position.field_1350);
        } else {
            method_19324((float) (.22 * forward), 0, 0);
        }
        method_19325(camera.method_19330()+(float)shake, (EntryTransition.ritualActive()?-90:camera.method_19329()) + (float)shake + (float) (-3 * up + 1.5 * forward));
    }
}
