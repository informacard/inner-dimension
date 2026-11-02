package com.innerdimension.network;
import net.minecraft.*;
/** Selects the newly unlocked design without opening a screen during the ritual. */
public record SelectCrestSlotPayload(int slot) implements class_8710 {
    public static final class_8710.class_9154<SelectCrestSlotPayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","select_crest_slot"));
    public static final class_9139<class_9129,SelectCrestSlotPayload> CODEC=class_9139.method_56437((buf,p)->buf.writeInt(p.slot),buf->new SelectCrestSlotPayload(buf.readInt()));
    public class_8710.class_9154<SelectCrestSlotPayload> method_56479(){return ID;}
}
