import com.innerdimension.voiddomain.ImpalementFallTrace;
public final class ImpalementFallTraceRegression {
 private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  var trace=new ImpalementFallTrace();
  trace.accept(30,29,false,false);trace.accept(29,25,false,false);
  // Creative mode supplies no vanilla fallDamage callback/fallDistance. Physical descent still counts.
  double landed=trace.accept(25,8,true,false);
  check(Math.abs(landed-22)<1e-8&&ImpalementFallTrace.highEnough(landed),"22-block landing survives vanilla fall-distance reset");
  check(!ImpalementFallTrace.highEnough(21.99),"short fall rejected");
  check(ImpalementFallTrace.highEnough(21.999999),"floating point boundary");
  check(trace.accept(8,8,true,false)==0,"grounded packets cannot retrigger");
  trace.accept(100,78,false,true);
  check(trace.accept(78,77,true,false)==1,"creative flight cannot count toward ritual");
  trace.accept(50,40,false,false);trace.accept(40,45,false,false);
  check(trace.accept(45,30,true,false)==15,"upward movement resets fall height");
  trace.accept(100,90,false,false);
  check(trace.accept(30,20,true,false)==10,"discontinuous positions cannot count as one fall");
  System.out.println("PASS: impalement 22-block physical landing, creative descent, flight exclusion, landing reset, upward reset, teleport discontinuity");
 }
}
