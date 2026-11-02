package com.innerdimension.network;
import com.innerdimension.projection.*;
import net.minecraft.*;

public record SaveFloorPatternPayload(FloorPatternSettings settings) implements class_8710 {
    public static final class_8710.class_9154<SaveFloorPatternPayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","save_floor_pattern"));
    public static final class_9139<class_9129,SaveFloorPatternPayload> CODEC=class_9139.method_56437((b,p)->write(b,p.settings),b->new SaveFloorPatternPayload(read(b)));
    public static void write(class_9129 b,FloorPatternSettings s) {
        b.method_10788(s.mode().name(),32);b.method_10788(s.expression(),512);b.writeBoolean(s.preserveCenter());
        b.method_10788(s.foreground(),128);b.method_10788(s.background(),128);
    }
    public static FloorPatternSettings read(class_9129 b) {
        return new FloorPatternSettings(FloorPatternSettings.Mode.valueOf(b.method_10800(32)),b.method_10800(512),b.readBoolean(),b.method_10800(128),b.method_10800(128));
    }
    @Override public class_8710.class_9154<SaveFloorPatternPayload> method_56479(){return ID;}
}
