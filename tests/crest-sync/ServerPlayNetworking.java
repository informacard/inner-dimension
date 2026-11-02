package net.fabricmc.fabric.api.networking.v1;
import com.innerdimension.network.DomainVisualStatePayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_3222;
public final class ServerPlayNetworking {
    public record Sent(class_3222 recipient, DomainVisualStatePayload payload) {}
    public static final List<Sent> SENT = new ArrayList<>();
    public static boolean canSend(class_3222 player, Object id) { return player.canSend; }
    public static void send(class_3222 player, DomainVisualStatePayload payload) { SENT.add(new Sent(player, payload)); }
}
