package com.innerdimension.projection.builtin;
import java.util.*;
import java.util.function.Supplier;
public final class SuperByteBufferCache {
 public static final class Compartment<K>{}
 private static final SuperByteBufferCache INSTANCE=new SuperByteBufferCache();
 private final Map<Compartment<?>,Map<Object,SuperByteBuffer>> buffers=new IdentityHashMap<>();
 public static SuperByteBufferCache getInstance(){return INSTANCE;}
 public <K> SuperByteBuffer get(Compartment<K> c,K key,Supplier<SuperByteBuffer> factory){return buffers.computeIfAbsent(c,x->new HashMap<>()).computeIfAbsent(key,x->factory.get());}
 public <K> void invalidate(Compartment<K> c,K key){var map=buffers.get(c);if(map!=null){var old=map.remove(key);if(old!=null)old.delete();}}
 public void clear(){buffers.values().forEach(m->m.values().forEach(SuperByteBuffer::delete));buffers.clear();}
}
