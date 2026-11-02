import com.innerdimension.client.DomainSubtitleAnimation;
import java.util.Arrays;

public final class DomainSubtitleRegression {
    private static void check(boolean condition) { if (!condition) throw new AssertionError(); }
    public static void main(String[] args) {
        check(Arrays.equals(DomainSubtitleAnimation.nameUnits("无量空处"), new String[]{"「", "无", "量", "空", "处", "」"}));
        check(Arrays.equals(DomainSubtitleAnimation.nameUnits("Unlimited Void"), new String[]{"「", "Unlimited", " Void", "」"}));
        check(DomainSubtitleAnimation.characters("领域展开").length == 4);
        check(DomainSubtitleAnimation.characters("𠀀").length == 1);
        for (int duration : new int[]{1, 5, 15, 30}) {
            for (int i = 0; i < 4; i++) {
                check(DomainSubtitleAnimation.reveal(0, duration, i, 4) == 0);
                check(DomainSubtitleAnimation.reveal(duration, duration, i, 4) == 0);
                check(Math.abs(DomainSubtitleAnimation.reveal(duration*.5, duration, i, 4)-1)<1e-9);
                check(Math.abs(DomainSubtitleAnimation.reveal(duration*.75, duration, i, 4)-1)<1e-9);
                check(DomainSubtitleAnimation.reveal(duration*.9, duration, i, 4)>0);
                check(DomainSubtitleAnimation.reveal(duration*.9, duration, i, 4)<1);
            }
        }
        check(DomainSubtitleAnimation.gap(0) == 0);
        check(DomainSubtitleAnimation.alpha(0) == 1);
        check(DomainSubtitleAnimation.alpha(10) == 1);
        check(DomainSubtitleAnimation.alpha(20) == 0);
        double previousGap = -1, previousAlpha = 1;
        for (int i = 0; i <= 200; i++) {
            double tick = i / 10.0;
            double gap = DomainSubtitleAnimation.gap(tick);
            double alpha = DomainSubtitleAnimation.alpha(tick);
            check(gap >= previousGap && alpha <= previousAlpha && alpha >= 0);
            previousGap = gap; previousAlpha = alpha;
        }
        double slow = DomainSubtitleAnimation.gap(1) - DomainSubtitleAnimation.gap(0);
        double fast = DomainSubtitleAnimation.gap(15) - DomainSubtitleAnimation.gap(14);
        check(fast > slow * 5);
        check(DomainSubtitleAnimation.gap(.001) < .00001);
        System.out.println("Domain subtitle regression passed");
    }
}
