package com.innerdimension.mixin;
import com.innerdimension.projection.ProjectionEnvironment;
import com.innerdimension.projection.ProjectionSpawnPlacement;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Spawn eggs retain vanilla initialization, but use the actual projected face and mixed collision. */
@Mixin(class_1826.class)
public abstract class ProjectionSpawnEggMixin {
    @Inject(method = "method_7884", at = @At("HEAD"), cancellable = true)
    private void inner$projectedSpawn(class_1838 context, CallbackInfoReturnable<class_1269> cir) {
        class_243 hit = context.method_17698();
        class_238 sample = new class_238(hit,hit).method_1014(0.025);
        if (ProjectionEnvironment.collisionShapes(context.method_8045(), sample).isEmpty()) return;
        if (!(context.method_8045() instanceof class_3218 world)) { cir.setReturnValue(class_1269.field_5812); return; }
        class_1799 stack = context.method_8041();
        class_1299<?> type = ((class_1826)(Object)this).method_8015(stack);
        if (type == null || !type.method_72390() && world.method_8407() == class_1267.field_5801) {
            cir.setReturnValue(class_1269.field_5814); return;
        }
        class_1297 entity = type.method_5888(world, class_1299.method_48009(world, stack, context.method_8036()),
                context.method_8037(), class_3730.field_16465, false, false);
        if (entity == null || !ProjectionSpawnPlacement.place(world, entity, hit, context.method_8038())) {
            cir.setReturnValue(class_1269.field_5814); return;
        }
        world.method_30771(entity);
        if (entity instanceof class_1308 mob) mob.method_5966();
        stack.method_57008(1, context.method_8036());
        world.method_33596(context.method_8036(), class_5712.field_28738, entity.method_24515());
        cir.setReturnValue(class_1269.field_5812);
    }
}
