package com.innerdimension.projection;
import net.minecraft.*;
/** Initial structure uses the same bounded atomic fragments as live updates. */
public record ProjectionInitialPayload(boolean shell,ProjectionSourceSpec spec,ProjectionUpdatePayload fragment) implements class_8710 {
 public static final class_8710.class_9154<ProjectionInitialPayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","projection_initial"));
 public static final class_9139<class_9129,ProjectionInitialPayload> CODEC=class_9139.method_56437((buf,p)->{
  buf.writeBoolean(p.shell);writeVolume(buf,p.spec.source());buf.writeBoolean(p.spec.shell()!=null);if(p.spec.shell()!=null)writeVolume(buf,p.spec.shell());
  buf.writeBoolean(p.spec.mirror()!=null);if(p.spec.mirror()!=null)writeVolume(buf,p.spec.mirror());
  buf.writeDouble(p.spec.anchorX());buf.writeDouble(p.spec.anchorY());buf.writeDouble(p.spec.anchorZ());ProjectionUpdatePayload.CODEC.encode(buf,p.fragment);
 },buf->{boolean shell=buf.readBoolean();var source=readVolume(buf);var walls=buf.readBoolean()?readVolume(buf):null;var mirror=buf.readBoolean()?readVolume(buf):null;
  double x=buf.readDouble(),y=buf.readDouble(),z=buf.readDouble();if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z))throw new IllegalArgumentException("Invalid projection anchor");
  return new ProjectionInitialPayload(shell,new ProjectionSourceSpec(source,walls,mirror,x,y,z),ProjectionUpdatePayload.CODEC.decode(buf));});
 private static void writeVolume(class_9129 b,ProjectionSourceSpec.Volume v){b.writeInt(v.minX());b.writeInt(v.minY());b.writeInt(v.minZ());b.writeInt(v.maxX());b.writeInt(v.maxY());b.writeInt(v.maxZ());}
 private static ProjectionSourceSpec.Volume readVolume(class_9129 b){return new ProjectionSourceSpec.Volume(b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readInt());}
 public class_8710.class_9154<ProjectionInitialPayload> method_56479(){return ID;}
}
