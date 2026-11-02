package com.innerdimension.projection.builtin.world;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_238;
import net.minecraft.class_5568;
import net.minecraft.class_5575;
import net.minecraft.class_5577;
import net.minecraft.class_7927;

@Environment(EnvType.CLIENT)
public class VirtualLevelEntityGetter<T extends class_5568> implements class_5577<T> {
   public T method_31804(int id) {
      return null;
   }

   public T method_31808(UUID uuid) {
      return null;
   }

   public Iterable<T> method_31803() {
      return Collections.emptyList();
   }

   public <U extends T> void method_31806(class_5575<T, U> test, class_7927<U> consumer) {
   }

   public void method_31807(class_238 boundingBox, Consumer<T> consumer) {
   }

   public <U extends T> void method_31805(class_5575<T, U> test, class_238 bounds, class_7927<U> consumer) {
   }
}
