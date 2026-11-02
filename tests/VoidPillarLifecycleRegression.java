import com.innerdimension.voiddomain.VoidEmbodimentRules;

/** Regression for death, respawn and save-reload column generation policy; no game launch. */
public final class VoidPillarLifecycleRegression {
    private static void check(boolean condition,String reason){if(!condition)throw new AssertionError(reason);}
    public static void main(String[] args){
        check(!VoidEmbodimentRules.pillars(0),"No columns before impalement");
        check(VoidEmbodimentRules.pillars(1),"Living embodied owner retains both columns");
        int persistedAfterDeath=VoidEmbodimentRules.afterDeath(1);
        check(!VoidEmbodimentRules.embodied(persistedAfterDeath),"Death releases active-state landing guard, allowing another impalement");
        check(!VoidEmbodimentRules.pillars(persistedAfterDeath),"Death removes columns");
        check(!VoidEmbodimentRules.pillars(persistedAfterDeath),"Respawn with persisted phase must not regenerate columns");
        check(!VoidEmbodimentRules.pillars(3),"Completed baptism has no columns");
        check(!VoidEmbodimentRules.pillars(4),"Pending baptism film must not regenerate columns");
        check(VoidEmbodimentRules.pillars(1),"A new successful impalement can create columns again");
        check(VoidEmbodimentRules.afterDeath(4)==2,"Dying during unfinished baptism must cancel pending completion");
        check(VoidEmbodimentRules.afterDeath(3)==3,"Death preserves already completed baptism");
        System.out.println("PASS VoidPillarLifecycleRegression: before ritual, active ritual, death, respawn, baptism and repeat ritual");
    }
}
