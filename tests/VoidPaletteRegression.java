import com.innerdimension.client.VoidTexturePalette;
import javax.imageio.ImageIO;
import java.io.File;
public class VoidPaletteRegression {
 public static void main(String[] args)throws Exception {
  for(String name:new String[]{"void_water","void_mist"}) {
   var image=ImageIO.read(new File("dev/inner_dimension/resources/assets/inner_dimension/textures/block/"+name+".png"));
   int[] original=image.getRGB(0,0,image.getWidth(),image.getHeight(),null,0,image.getWidth());
   int[] pixels=original.clone();VoidTexturePalette.apply(pixels);int black=0,gray=0,max=0;
   for(int i=0;i<pixels.length;i++) {
    if((pixels[i]>>>24)!=(original[i]>>>24))throw new AssertionError("alpha changed");
    int r=(pixels[i]>>>16)&255,g=(pixels[i]>>>8)&255,b=pixels[i]&255;
    if(r!=g||r!=b)throw new AssertionError("colored pattern");
    if(r==0)black++;else gray++;max=Math.max(max,r);
   }
   if(black*10L<pixels.length*7L||gray==0||max!=128)throw new AssertionError("palette is not black dominated with gray detail");
   System.out.println("PASS "+name+": pure black="+black+"/"+pixels.length+", gray detail="+gray+", brightest gray="+max);
  }
 }
}
