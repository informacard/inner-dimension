import com.innerdimension.domain.ability.D59;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.util.*;
import java.lang.reflect.*;
import sun.misc.Unsafe;

public class Domain141Regression {
    static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[] args)throws Exception {
        DomainCreatureRegression.main(args);
        Unsafe u=DomainCreatureRegression.unsafe;
        DomainSession s=(DomainSession)u.allocateInstance(DomainSession.class);
        UUID owner=UUID.randomUUID();
        DomainCreatureRegression.field(s,DomainSession.class,"ownerId",owner);
        var enemy=DomainCreatureRegression.mob("zombie");
        check(D59.INSTANCE.healingMultiplier(s,enemy)==0,"Enemy healing completely blocked");
        var boss=DomainCreatureRegression.mob("warden");
        check(D59.INSTANCE.healingMultiplier(s,boss)==0,"Boss healing completely blocked");
        var neutral=DomainCreatureRegression.mob("cow");
        check(D59.INSTANCE.healingMultiplier(s,neutral)==1,"Neutral healing preserved");
        enemy.uuid=owner;
        check(D59.INSTANCE.healingMultiplier(s,enemy)==1,"Owner healing preserved");
        var ally=DomainCreatureRegression.mob("zombie");
        ally.uuid=owner; check(D59.INSTANCE.healingMultiplier(s,ally)==1,"Friendly owner relation preserved");
        var item=(class_1542)u.allocateInstance(class_1542.class);
        item.method_6982(10);
        check(item.method_6977(),"Fresh drops wait for pickup delay");
        item.method_6975();
        DomainCreatureRegression.field(item,class_1542.class,"field_7204",200);
        check(item.method_6985()==200 && !item.method_6977(),"Older eligible drops remain collectible");
        check(DomainCreatureRegression.calls(DomainCreatureRegression.node("domain/ability/D23"),"method_6977"),"D23 checks pickup delay");
        check(!DomainCreatureRegression.calls(DomainCreatureRegression.node("domain/ability/D23"),"method_6985"),"D23 no longer filters item age");
        check(DomainCreatureRegression.calls(DomainCreatureRegression.node("mixin/ProjectionSneakSupportMixin"),"projectedCollision"),"Sneak edge checks include projected floors");
        ClassNode vanilla=new ClassNode();
        try(var in=class_1657.class.getResourceAsStream("/net/minecraft/class_1657.class")){new ClassReader(in).accept(vanilla,0);}
        check(vanilla.methods.stream().anyMatch(m->m.name.equals("method_59818")&&m.desc.equals("(DDD)Z")),"Sneak injection matches runtime descriptor");
        Field multiplier=D59.class.getDeclaredField("HEAL_MULTIPLIER");multiplier.setAccessible(true);
        check(multiplier.getFloat(null)==0,"D59 configured for 100% reduction");
        System.out.println("PASS: 1.4.1 enemy/boss/self/neutral healing; delayed and aged drops; pickup integration; projected sneak support; runtime injection target");
    }
}

