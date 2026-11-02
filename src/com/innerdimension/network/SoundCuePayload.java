package com.innerdimension.network;
import net.minecraft.*;
public record SoundCuePayload(String key,float volume) implements class_8710 {
    public static final class_8710.class_9154<SoundCuePayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","sound_cue"));
    public static final class_9139<class_9129,SoundCuePayload> CODEC=class_9139.method_56437((buf,p)->{buf.method_10814(p.key);buf.writeFloat(p.volume);},buf->new SoundCuePayload(buf.method_19772(),buf.readFloat()));
    public class_8710.class_9154<SoundCuePayload> method_56479(){return ID;}
}
