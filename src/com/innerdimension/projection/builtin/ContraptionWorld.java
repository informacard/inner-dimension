package com.innerdimension.projection.builtin;
import net.minecraft.*;
public class ContraptionWorld implements class_1922 {
 protected final class_1937 world;protected final Contraption snapshot;
 public ContraptionWorld(class_1937 world,Contraption snapshot){this.world=world;this.snapshot=snapshot;}
 public class_2680 method_8320(class_2338 pos){var info=snapshot.getBlocks().get(pos);return info==null?class_2246.field_10124.method_9564():info.comp_1342();}
 public class_3610 method_8316(class_2338 pos){return method_8320(pos).method_26227();}
 public class_2586 method_8321(class_2338 pos){return null;}
 public int method_31605(){return (int)(snapshot.bounds.field_1325-snapshot.bounds.field_1322);}
 public int method_31607(){return (int)snapshot.bounds.field_1322;}
}
