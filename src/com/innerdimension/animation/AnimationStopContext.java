package com.innerdimension.animation;

import java.util.UUID;
import net.minecraft.class_3218;

/** Context shared between NoN's injected entry point and its transform arguments. */
public record AnimationStopContext(class_3218 world, UUID instance, boolean natural) {}
