import com.innerdimension.domain.ability.D35;
import com.innerdimension.domain.ability.D41;
import com.innerdimension.domain.ability.D48;
import com.innerdimension.domain.ability.KnockbackHook;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.lang.reflect.*;
import sun.misc.Unsafe;
/** Real vanilla glideability and domain shell edge regression, without modifying any save. */
public final class DomainMovementBlastRegression {
    static void check(boolean value, String label) { if (!value) throw new AssertionError(label); }
    public static void main(String[] args) throws Exception {
        class_155.method_36208();
        class_2966.method_12851();
        class_1799 wings = D41.wings();
        check(class_1309.method_63624(wings, class_1304.field_48824), "BODY wings must satisfy vanilla canGlide");
        check(!class_1309.method_63624(wings, class_1304.field_6174), "Wings must not consume CHEST");
        wings.method_7974(1);
        check(class_1309.method_63624(D41.wings(), class_1304.field_48824), "Virtual wings cannot accumulate damage");
        check(!KnockbackHook.class.isInstance(D48.INSTANCE), "Breeze must not alter attack knockback");
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe"); unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        DomainSession session = (DomainSession) unsafe.allocateInstance(DomainSession.class);
        for (String name : new String[]{"minX", "minY", "minZ", "maxX", "maxY", "maxZ"}) {
            Field field = DomainSession.class.getDeclaredField(name); field.setAccessible(true);
            field.setDouble(session, name.startsWith("min") ? 1 : 9);
        }
        Method block = D35.class.getDeclaredMethod("inShell", DomainSession.class, class_2338.class);
        Method point = D35.class.getDeclaredMethod("insideShell", DomainSession.class, class_243.class);
        block.setAccessible(true); point.setAccessible(true);
        check((boolean) block.invoke(null, session, new class_2338(4,0,4)), "Protect floor below interior");
        check((boolean) block.invoke(null, session, new class_2338(0,4,4)), "Protect shell wall");
        check((boolean) block.invoke(null, session, new class_2338(9,9,9)), "Protect far boundary corner");
        check(!(boolean) block.invoke(null, session, new class_2338(10,4,4)), "Outside block remains outside");
        check(!(boolean) block.invoke(null, session, new class_2338(-1,4,4)), "Negative outside block remains outside");
        check((boolean) point.invoke(null, session, new class_243(9.999,4,4)), "Blast immediately inside shell is contained");
        check(!(boolean) point.invoke(null, session, new class_243(10,4,4)), "Blast on outer face is external");
        Field codes = DomainSession.class.getDeclaredField("codes"); codes.setAccessible(true);
        codes.set(session, java.util.Set.of());
        Method policy = D35.class.getDeclaredMethod("blockedBy", DomainSession.class, class_243.class, class_2338.class);
        policy.setAccessible(true);
        class_243 internal = new class_243(5,5,5), external = new class_243(12,5,5);
        class_2338 innerBlock = new class_2338(5,5,5), outerBlock = new class_2338(11,5,5);
        check((boolean) policy.invoke(null, session, internal, outerBlock), "Every domain contains outgoing explosions");
        check(!(boolean) policy.invoke(null, session, internal, innerBlock), "Without D35 vanilla can destroy internal blocks");
        check(!(boolean) policy.invoke(null, session, external, outerBlock), "External blasts retain vanilla behavior outside");
        codes.set(session, java.util.Set.of("D35"));
        check((boolean) policy.invoke(null, session, internal, innerBlock), "D35 blocks internal destruction");
        check((boolean) policy.invoke(null, session, external, innerBlock), "D35 also protects against external explosions");
        check((boolean) policy.invoke(null, session, internal, outerBlock), "D35 cannot leak a blast through the shell");
        System.out.println("PASS: vanilla BODY glideability, independent chest slot, durability, breeze hook removal, floor/wall/corner/outside shell bounds");
    }
}


