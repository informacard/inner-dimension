import com.innerdimension.item.ExtractionTiming;
public final class ExtractionTimingRegression {
 public static void main(String[] args){
  for(int i=0;i<20;i++)if(ExtractionTiming.complete(i))throw new AssertionError("Early payout "+i);
  if(!ExtractionTiming.complete(20)||!ExtractionTiming.complete(21))throw new AssertionError("One second completion");
  if(ExtractionTiming.fovScale(0)!=1||ExtractionTiming.fovScale(20)!=.75f)throw new AssertionError("FOV endpoints");
  float previous=1;
  for(int i=0;i<=80;i++){float current=ExtractionTiming.fovScale(i/4f);if(current>previous)throw new AssertionError("Non-monotonic FOV");previous=current;}
  if(ExtractionTiming.fovScale(-1)!=1||ExtractionTiming.fovScale(40)!=.75f)throw new AssertionError("FOV bounds");
  System.out.println("Extraction timing and FOV regression passed");
 }
}
