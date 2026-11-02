import com.innerdimension.client.crest.*;
import com.innerdimension.progression.DomainCapacityRules;
import java.awt.image.BufferedImage;import java.io.*;import java.nio.file.*;import java.util.*;import javax.imageio.ImageIO;
public final class NextPhaseRegression {
 static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
 static void reset()throws Exception{var loaded=CrestDesignStore.class.getDeclaredField("loaded");loaded.setAccessible(true);loaded.setBoolean(null,false);}
 static String png(BufferedImage image)throws Exception{var out=new ByteArrayOutputStream();ImageIO.write(image,"png",out);return Base64.getEncoder().encodeToString(out.toByteArray());}
 public static void main(String[] args)throws Exception{
  Path dir=Path.of(System.getProperty("test.config"));Files.createDirectories(dir);
  BufferedImage custom=new BufferedImage(24,16,BufferedImage.TYPE_INT_ARGB);custom.setRGB(8,9,0xff123abc);
  BufferedImage fine=new BufferedImage(48,32,BufferedImage.TYPE_INT_ARGB);fine.setRGB(17,19,0xffdef012);
  BufferedImage fallback=new BufferedImage(24,16,BufferedImage.TYPE_INT_ARGB);fallback.setRGB(8,9,0xffff0000);
  Properties old=new Properties();old.setProperty("storage.version","2");old.setProperty("slot.0",png(custom));old.setProperty("slot.1",png(fine));old.setProperty("slot.1.resolution","512");old.setProperty("slot.2",png(custom));try(var out=Files.newOutputStream(dir.resolve("inner_dimension_crest.properties"))){old.store(out,"test");}
  byte[] original=Files.readAllBytes(dir.resolve("inner_dimension_crest.properties"));
  CrestDesignStore.load(fallback);
  check(CrestDesignStore.slot(0).getRGB(8,9)==0xff123abc,"Migration overwrote custom artwork");
  check(CrestDesignStore.slot(1).getRGB(17,19)==0xffdef012,"512 pixels lost");
  check(Arrays.equals(original,Files.readAllBytes(dir.resolve("inner_dimension/legacy-crests.properties.bak"))),"Legacy backup is not byte-exact");
  check(!Files.exists(dir.resolve("inner_dimension_crest.properties")),"Legacy file still active");
  check(Files.list(dir.resolve("inner_dimension")).filter(p->p.toString().endsWith(".png")).count()==6,"Missing slots");
  CrestDesignStore.save(5,fine,512);CrestDesignStore.rename(5,"Across worlds 六槽");CrestDesignStore.rename(0,"");
  reset();CrestDesignStore.load(fallback);CrestDesignStore.importNames(List.of("old world name","","","","","different world name"));
  check(CrestDesignStore.selected()==5,"Sixth selection lost on reload");check(CrestDesignStore.slot(5).getRGB(17,19)==0xffdef012,"Sixth artwork lost on reload");check(CrestDesignStore.name(5).equals("Across worlds 六槽"),"Other world overwrote name");check(CrestDesignStore.name(0).isEmpty(),"Cleared name resurrected from old world");
  Path png=dir.resolve("inner_dimension/crest-1.png");Files.write(png,new byte[]{1,2,3});byte[] metadata=Files.readAllBytes(dir.resolve("inner_dimension/crests.properties"));reset();boolean refused=false;try{CrestDesignStore.load(fallback);}catch(IllegalStateException expected){refused=true;}
  check(refused,"Corrupt artwork silently became default");check(Arrays.equals(Files.readAllBytes(png),new byte[]{1,2,3}),"Corrupt original overwritten");check(Arrays.equals(metadata,Files.readAllBytes(dir.resolve("inner_dimension/crests.properties"))),"Metadata overwritten after failed read");
  check(DomainCapacityRules.capacity(0)==6,"Initial capacity");check(DomainCapacityRules.settle(0,44,true,false)==0,"Seeding gate");check(DomainCapacityRules.settle(0,44,false,true)==0,"Overload gate");int settled=DomainCapacityRules.settle(0,44,true,true);check(settled==1&&DomainCapacityRules.capacity(settled)==9,"Historical first settlement");settled=DomainCapacityRules.settle(settled,44,true,true);check(settled==2,"Unsettled historical milestone lost");check(DomainCapacityRules.settle(settled,44,true,true)==2,"Repeated overload inflated capacity");check(DomainCapacityRules.settle(settled,45,true,true)==3,"15 species boundary");check(DomainCapacityRules.settle(3,30,true,true)==3,"Settlement regressed");
  System.out.println("PASS: artwork migration, six-slot restart, cross-world names, corruption refusal, capacity unlock/history/boundaries/idempotence");
 }
}
