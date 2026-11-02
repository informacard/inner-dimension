package com.innerdimension.mixin;

import java.util.Map;
import java.util.concurrent.Executor;
import net.minecraft.class_1937;
import net.minecraft.class_3218;
import net.minecraft.class_32.class_5143;
import net.minecraft.class_5219;
import net.minecraft.class_5321;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    @Accessor("field_4589")
    Map<class_5321<class_1937>, class_3218> inner$getWorlds();

    @Accessor("field_17200")
    Executor inner$getExecutor();

    @Accessor("field_23784")
    class_5143 inner$getSession();

    @Accessor("field_24372")
    class_5219 inner$getSaveProperties();
}
