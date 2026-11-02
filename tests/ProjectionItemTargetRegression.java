import com.innerdimension.projection.ProjectionItemTarget;
import net.minecraft.*;

public final class ProjectionItemTargetRegression {
    private static int checks;
    private static void check(boolean value) {
        checks++;
        if (!value) throw new AssertionError("Check " + checks);
    }
    private static void target(double x, double y, double z, class_2350 side, int bx, int by, int bz) {
        var hit = ProjectionItemTarget.worldHit(new class_243(x, y, z), side);
        check(hit.method_17777().equals(new class_2338(bx, by, bz)));
        check(hit.method_17780() == side);
        var p = hit.method_17784();
        check(p.field_1352 >= bx && p.field_1352 <= bx + 1
                && p.field_1351 >= by && p.field_1351 <= by + 1
                && p.field_1350 >= bz && p.field_1350 <= bz + 1);
    }
    public static void main(String[] args) {
        class_155.method_36208(); class_2966.method_12851();
        target(.25, 64.01, .75, class_2350.field_11036, 0, 64, 0);
        target(.25, 64, .75, class_2350.field_11033, 0, 63, 0);
        for (class_2350 side : class_2350.values()) {
            target(.5 + side.method_10148() * .5, 64.5 + side.method_10164() * .5,
                    .5 + side.method_10165() * .5, side,
                    side.method_10148(), 64 + side.method_10164(), side.method_10165());
        }
        target(-1.8, -3.01, -4.2, class_2350.field_11036, -2, -4, -5);
        target(-1.8, -3.01, -4.2, class_2350.field_11033, -2, -4, -5);
        target(.75, 64.5, .25, class_2350.field_11036, 0, 64, 0);
        // Fractional walls must choose the containing cell on every face,
        // including negative coordinates and anchors close to grid boundaries.
        for (class_2350 side : class_2350.values()) {
            for (double fraction : new double[]{.01, .25, .5, .75, .99}) {
                target(fraction, 64 + fraction, fraction, side, 0, 64, 0);
                target(-2 + fraction, -4 + fraction, -5 + fraction, side, -2, -4, -5);
            }
        }
        System.out.println("ProjectionItemTargetRegression: " + checks + " checks passed");
    }
}
