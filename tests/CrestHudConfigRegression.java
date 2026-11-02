import com.innerdimension.config.InnerConfig;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CrestHudConfigRegression {
    private static void check(boolean ok){if(!ok)throw new AssertionError("HUD config mismatch");}
    public static void main(String[] args)throws Exception{
        Path dir=Path.of(args[0]);Files.createDirectories(dir);
        Path file=dir.resolve("inner_dimension.properties");
        Files.writeString(file,"");InnerConfig.load(dir);
        var cfg=InnerConfig.get();
        check(cfg.crestHudOffsetX==0&&cfg.crestHudHeightFromBottom==53&&cfg.crestHudScale==1&&cfg.crestHudIdleOpacity==30&&cfg.projectionShellTicks==15&&cfg.projectionInteriorTicks==5&&!cfg.projectionLavaDamage);
        cfg.crestHudOffsetX=-30;cfg.crestHudHeightFromBottom=44;cfg.crestHudScale=1.5;cfg.crestHudIdleOpacity=40;
        InnerConfig.save(dir);cfg.crestHudScale=1;InnerConfig.load(dir);
        check(cfg.crestHudOffsetX==-30&&cfg.crestHudHeightFromBottom==44&&cfg.crestHudScale==1.5&&cfg.crestHudIdleOpacity==40);
        Files.writeString(file,"crestHudOffsetX=-9999\ncrestHudHeightFromBottom=-10\ncrestHudScale=NaN\ncrestHudIdleOpacity=999\n");
        InnerConfig.load(dir);
        check(cfg.crestHudOffsetX==-1000&&cfg.crestHudHeightFromBottom==0&&cfg.crestHudScale==1&&cfg.crestHudIdleOpacity==100);
        System.out.println("HUD config defaults, persistence and bounds passed");
    }
}
