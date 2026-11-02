import com.innerdimension.domain.runtime.AnimationCompletionTracker;
import com.innerdimension.domain.runtime.AnimationCompletionTracker.Observation;
import java.util.*;
public final class AnimationCompletionRegression {
 static final UUID OWNER=UUID.randomUUID(),A=UUID.randomUUID(),B=UUID.randomUUID(),C=UUID.randomUUID();
 static void check(boolean result,String reason){if(!result)throw new AssertionError(reason);}
 static Observation animation(String name,String predecessor,UUID... actors){return new Observation(UUID.randomUUID(),name,Set.of(actors),predecessor);}
 public static void main(String[] args){
  var tracker=new AnimationCompletionTracker();var one=animation("one",null,OWNER,A);var two=animation("two","one",OWNER,A,B);var three=animation("three","two",OWNER,A,B,C);
  check(tracker.update(OWNER,List.of(one),id->true,0).isEmpty(),"Active 1v1 cannot complete");
  check(tracker.update(OWNER,List.of(two),id->true,1).isEmpty(),"1v1 -> 1v2 is a join replacement");
  check(tracker.update(OWNER,List.of(three),id->true,2).isEmpty(),"Consecutive 1v2 -> 1v3 also transfers");
  check(tracker.update(OWNER,List.of(),id->true,3).isEmpty(),"Completion waits one tick for published replacement");
  check(tracker.update(OWNER,List.of(),id->true,4).equals(Set.of(A,B,C)),"True final end completes every non-owner participant");
  check(tracker.update(OWNER,List.of(),id->true,5).isEmpty(),"End is delivered only once");
  tracker=new AnimationCompletionTracker();tracker.update(OWNER,List.of(one),id->true,10);
  check(tracker.update(OWNER,List.of(),id->true,11).isEmpty(),"Tick boundary between STOP and START is tolerated");
  check(tracker.update(OWNER,List.of(two),id->true,12).isEmpty(),"Delayed explicit successor cancels old completion");
  tracker=new AnimationCompletionTracker();tracker.update(OWNER,List.of(one),id->true,20);
  var independent=animation("two",null,OWNER,A,B);tracker.update(OWNER,List.of(independent),id->true,21);
  check(tracker.update(OWNER,List.of(independent),id->true,22).equals(Set.of(A)),"An unrelated restart must not suppress real completion");
  tracker=new AnimationCompletionTracker();tracker.update(OWNER,List.of(one),id->true,30);
  var wrongPredecessor=animation("two","wrong",OWNER,A,B);tracker.update(OWNER,List.of(wrongPredecessor),id->true,31);
  check(tracker.update(OWNER,List.of(wrongPredecessor),id->true,32).equals(Set.of(A)),"Wrong predecessor does not create a false handoff");
  tracker=new AnimationCompletionTracker();tracker.update(OWNER,List.of(animation("others",null,A,B)),id->true,40);tracker.update(OWNER,List.of(),id->true,41);
  check(tracker.update(OWNER,List.of(),id->true,42).isEmpty(),"Animations without owner are ignored");
  tracker=new AnimationCompletionTracker();tracker.update(OWNER,List.of(two),id->id.equals(A),50);tracker.update(OWNER,List.of(),id->true,51);
  check(tracker.update(OWNER,List.of(),id->true,52).equals(Set.of(A)),"Only eligible recorded creatures complete");
  System.out.println("PASS: join replacement, repeated joins, STOP/START tick gap, independent stop, predecessor matching, ownership and single completion");
 }
}