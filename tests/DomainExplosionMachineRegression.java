import com.innerdimension.domain.ability.D46;
import com.innerdimension.centrifuge.CentrifugeBlockEntity;
import com.innerdimension.centrifuge.CentrifugeBottleData;
import net.minecraft.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.lang.reflect.*;
import java.util.*;
import sun.misc.Unsafe;
/** Regressions for Lithium integration, real inventory drops, and ceiling tracking. */
public final class DomainExplosionMachineRegression {
    static void check(boolean value, String label) { if (!value) throw new AssertionError(label); }
    static void set(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field); f.setAccessible(true); f.set(target, value);
    }
    public static void main(String[] args) throws Exception {
        class_155.method_36208(); class_2966.method_12851();
        String path = "/com/innerdimension/mixin/DomainExplosionFilterMixin.class";
        ClassNode mixin = new ClassNode();
        try (var stream = DomainExplosionMachineRegression.class.getResourceAsStream(path)) {
            new ClassReader(stream).accept(mixin, 0);
        }
        boolean callerHook = false;
        for (MethodNode m : mixin.methods) {
            if (m.visibleAnnotations == null) continue;
            for (AnnotationNode a : m.visibleAnnotations) {
                if (!a.desc.endsWith("/ModifyExpressionValue;")) continue;
                String annotation = String.valueOf(a.values);
                // Do not hook either the skipped vanilla scanning loop or its RETURN handlers.
                if (annotation.contains("method_61737")) {
                    for (Object item : a.values) {
                        if (item instanceof List<?> list) for (Object value : list) {
                            if (value instanceof AnnotationNode at)
                                callerHook |= String.valueOf(at.values).contains("method_61740()Ljava/util/List;");
                        }
                        if (item instanceof AnnotationNode at)
                            callerHook |= String.valueOf(at.values).contains("method_61740()Ljava/util/List;");
                    }
                }
            }
        }
        check(callerHook, "Filter the completed explosion result in its caller after Lithium collects blocks");
        Field uf = Unsafe.class.getDeclaredField("theUnsafe"); uf.setAccessible(true);
        CentrifugeBlockEntity machine = (CentrifugeBlockEntity)((Unsafe)uf.get(null)).allocateInstance(CentrifugeBlockEntity.class);
        class_1799 gunpowder = new class_1799(class_1802.field_8639, 2);
        set(machine, "slots", new class_1799[]{class_1799.field_8037, gunpowder, new class_1799(class_1802.field_8469, 3)});
        set(machine, "liquids", new LinkedHashMap<class_2960,Integer>());
        set(machine, "mixedLiquids", new LinkedHashMap<class_2960,Integer>());
        set(machine, "energy", 7); set(machine, "bottleMl", 250);
        check(machine.method_5439() == 3, "Only three real slots are visible to vanilla inventory scatter");
        check(machine.method_5438(3).method_7960(), "Snapshot cannot be dropped as slot 3");
        check(machine.method_5438(1) == gunpowder, "Real contents remain available to drops");
        class_1799 snapshot = machine.storageSnapshot();
        check(!snapshot.method_7960() && snapshot.method_7947() == 8, "UI energy still synchronizes");
        check(CentrifugeBottleData.readStorage(snapshot).bottleMl() == 250, "Reservoir metadata still synchronizes");
        class_243 first = new class_243(2,4,6);
        check(!D46.moved(null, first), "First observation only records a position");
        check(!D46.moved(first, first), "Stationary enemies do not trigger");
        check(D46.moved(first, first.method_1031(0.001,0,0)), "Slow crouch movement triggers");
        check(D46.moved(first, first.method_1031(0,0.05,0)), "Vertical movement triggers");
        class_243 head = first.method_1031(1,1.5,3), source = D46.ceilingSource(19, head);
        check(source.field_1351 == 19 && source.field_1352 == head.field_1352 && source.field_1350 == head.field_1350,
                "Beam source tracks the head from the ceiling with no horizontal offset");
        System.out.println("PASS: post-Lithium explosion caller hook; real inventory versus UI metadata; stationary/crouch/vertical movement and ceiling tracking");
    }
}
