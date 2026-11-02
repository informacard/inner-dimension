package net.minecraft.server;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_3222;
public final class MinecraftServer {
    public final Players players = new Players();
    public Players method_3760() { return players; }
    public static final class Players {
        public final List<class_3222> online = new ArrayList<>();
        public List<class_3222> method_14571() { return online; }
    }
}
