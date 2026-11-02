import com.innerdimension.progression.RitualConditions;
import com.innerdimension.client.crest.CrestHudAppearance;
import com.innerdimension.pregnancy.MentalPregnancyChance;

public final class Guide142Regression {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        var none = RitualConditions.Destination.NONE;
        var passage = RitualConditions.Destination.PASSAGE;
        var domain = RitualConditions.Destination.DOMAIN;
        check(RitualConditions.destination(13.99,true,true,true)==none,"Below minimum");
        check(RitualConditions.destination(14,true,true,false)==passage,"One material at minimum");
        check(RitualConditions.destination(21.99,true,true,true)==passage,"Complete materials below core height");
        check(RitualConditions.destination(22,true,true,true)==domain,"Boundary opens domain");
        check(RitualConditions.destination(30,true,true,false)==passage,"High incomplete attempt remains discoverable");
        check(RitualConditions.destination(30,true,false,true)==passage,"Both single materials work");
        check(RitualConditions.destination(30,true,false,false)==none,"No materials cannot teleport");
        check(RitualConditions.destination(30,false,true,true)==none,"Gaze still required");
        check(RitualConditions.touches(22),"Touch at boundary");
        check(!RitualConditions.touches(21.99),"No early touch");
        check(Math.abs(RitualConditions.landingDistance(21.1,.9,0)-22)<1e-9,"Height measured to support");
        check(RitualConditions.landingDistance(22,64,64)==22,"Actual ground landing unchanged");
        check(RitualConditions.landingDistance(22,64.5,64.5)==22,"Raised support works");
        check(RitualConditions.landingDistance(14,64,64.5)==14,"No negative height adjustment");
        var idle=CrestHudAppearance.idle();
        check(CrestHudAppearance.fertile(idle,40,-1).equals(idle),"Absent effect preserves appearance");
        check(CrestHudAppearance.fertile(idle,0,0).equals(idle),"Breath starts at baseline");
        check(CrestHudAppearance.fertile(idle,60,1).equals(idle),"Three-second period");
        var first=CrestHudAppearance.fertile(idle,30,0);
        var second=CrestHudAppearance.fertile(idle,30,1);
        check(first.height()==idle.height()&&first.width()==idle.width()&&first.alpha()>idle.alpha(),"Fertility breathes without scaling");
        check(second.height()==idle.height()&&second.width()==idle.width()&&second.alpha()>first.alpha(),"II opacity visibly stronger, size unchanged");
        check(CrestHudAppearance.fertilitySway(20,-1)==0,"No effect means no sway");
        check(CrestHudAppearance.fertilitySway(15,1)>CrestHudAppearance.fertilitySway(15,0),"II sway stronger");
        check(Math.abs(CrestHudAppearance.fertilitySway(15,1)+CrestHudAppearance.fertilitySway(45,1))<1e-6,"Sway balanced around configured position");
        check(CrestHudAppearance.fertile(CrestHudAppearance.idle(1,100),40,1).alpha()==255,"Opacity clamps");
        check(CrestHudAppearance.fertile(CrestHudAppearance.idle(1,0),40,1).alpha()==0,"Hidden crest remains hidden");
        check(!MentalPregnancyChance.discoveryGuaranteed(49,false),"No premature guarantee");
        check(MentalPregnancyChance.discoveryGuaranteed(50,false),"First discovery eventually happens");
        check(!MentalPregnancyChance.discoveryGuaranteed(50,true),"Later pregnancies retain probability");
        System.out.println("PASS: ritual thresholds, surface height, incomplete materials, fertility breathing and discovery safeguard");
    }
}
