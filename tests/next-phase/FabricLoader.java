package net.fabricmc.loader.api;
import java.nio.file.*;
public final class FabricLoader {
 public static FabricLoader getInstance(){return new FabricLoader();}
 public Path getConfigDir(){return Path.of(System.getProperty("test.config"));}
}
