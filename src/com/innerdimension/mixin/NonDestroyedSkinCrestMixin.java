package com.innerdimension.mixin;
import com.innerdimension.client.crest.DomainCrestClient;
import com.nonid.client.NonDestroyedSkinClient;
import java.util.UUID;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=NonDestroyedSkinClient.class,remap=false)
public abstract class NonDestroyedSkinCrestMixin {
    @org.spongepowered.asm.mixin.Unique private static final ThreadLocal<UUID> inner$crestPlayer=new ThreadLocal<>();
    @Inject(method="getExternalPlayerVisualKey",at=@At("RETURN"),cancellable=true)
    private static void inner$crestVisualKey(class_1309 entity,CallbackInfoReturnable<Integer> cir){
        int crest=DomainCrestClient.active(entity.method_5667())?DomainCrestClient.revision(entity.method_5667()):0;
        cir.setReturnValue(31*cir.getReturnValue()+crest);
    }
    @Inject(method="getOrCreateTexture",at=@At("HEAD"))
    private static void inner$captureCrestPlayer(class_1309 entity,int stage,net.minecraft.class_2960 texture,boolean overlay,CallbackInfoReturnable<net.minecraft.class_2960> cir){inner$crestPlayer.set(entity==null?null:entity.method_5667());}
    @Inject(method="getOrCreateTexture",at=@At("RETURN"))
    private static void inner$releaseCrestPlayer(class_1309 entity,int stage,net.minecraft.class_2960 texture,boolean overlay,CallbackInfoReturnable<net.minecraft.class_2960> cir){inner$crestPlayer.remove();}
    @ModifyArg(method="getOrCreateTexture",at=@At(value="INVOKE",target="Lcom/nonid/client/NonDestroyedSkinClient;createCompositedSkin(Lnet/minecraft/class_310;Ljava/util/UUID;[BIZLnet/minecraft/class_2960;Lcom/nonid/client/NonExternalSkinCompat$SkinContext;Lcom/nonid/client/NonDestroyedSkinClient$MessState;Lcom/nonid/client/NonDestroyedSkinClient$TankMaskType;Ljava/util/List;)Lnet/minecraft/class_1011;"),index=2)
    private static byte[] inner$crestComposited(byte[] bytes){UUID id=inner$crestPlayer.get();return id==null?bytes:DomainCrestClient.derive(id,bytes);}
    @ModifyArg(method="getOrCreateTexture",at=@At(value="INVOKE",target="Lcom/nonid/client/NonDestroyedSkinClient;createMaskedOverlay(Lnet/minecraft/class_310;Ljava/util/UUID;[BI)Lnet/minecraft/class_1011;"),index=2)
    private static byte[] inner$crestMasked(byte[] bytes){UUID id=inner$crestPlayer.get();return id==null?bytes:DomainCrestClient.derive(id,bytes);}
}
