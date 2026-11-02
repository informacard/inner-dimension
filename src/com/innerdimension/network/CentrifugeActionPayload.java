package com.innerdimension.network;

import com.innerdimension.centrifuge.CentrifugeScreenHandler;
import net.minecraft.*;

/** An action names a species rather than a mutable row index. */
public record CentrifugeActionPayload(int syncId, String speciesId, boolean discard) implements class_8710 {
    public static final class_8710.class_9154<CentrifugeActionPayload> ID =
            new class_8710.class_9154<>(class_2960.method_60655("inner_dimension", "centrifuge_action"));
    public static final class_9139<class_9129, CentrifugeActionPayload> CODEC = class_9139.method_56437(
            (buf, p) -> { buf.writeInt(p.syncId); buf.method_10814(p.speciesId); buf.writeBoolean(p.discard); },
            buf -> new CentrifugeActionPayload(buf.readInt(), buf.method_19772(), buf.readBoolean()));
    @Override public class_8710.class_9154<CentrifugeActionPayload> method_56479() { return ID; }
    public static void receive(class_3222 player, CentrifugeActionPayload payload) {
        class_2960 species = class_2960.method_12829(payload.speciesId);
        if (species != null && player.field_7512 instanceof CentrifugeScreenHandler handler
                && handler.field_7763 == payload.syncId && handler.method_7597(player))
            handler.act(player, species, payload.discard);
    }
}
