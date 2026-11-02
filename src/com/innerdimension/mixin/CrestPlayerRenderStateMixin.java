package com.innerdimension.mixin;

import com.innerdimension.client.CrestPlayerRenderState;
import java.util.UUID;
import net.minecraft.class_10055;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(class_10055.class)
public abstract class CrestPlayerRenderStateMixin implements CrestPlayerRenderState {
    @Unique private UUID inner$crestPlayer;
    @Override public UUID inner$crestPlayer(){return inner$crestPlayer;}
    @Override public void inner$crestPlayer(UUID id){inner$crestPlayer=id;}
}
