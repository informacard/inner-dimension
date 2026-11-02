package com.innerdimension.domain;
import com.innerdimension.domain.runtime.DomainSession;
public final class DomainModes {
 public static final String SUCCUBUS="inner_dimension:succubus";
 public static final String SHRINE="inner_dimension:shrine";
 public static final String SLIME_LARGE="inner_dimension:slime_large",SLIME_SMALL="inner_dimension:slime_small";
 public static String sourceSpecies(String id){return id.equals(SLIME_LARGE)||id.equals(SLIME_SMALL)?"minecraft:slime":id;}
 public static String specialReverse(String id){return null;}
 public static boolean special(String id){return specialReverse(id)!=null;}
 public static boolean selectable(String code){return code.equals("D70")||code.equals("D37")||code.equals("D27")||code.equals("D21")||code.equals("D23");}
 public static boolean settings(String code){return selectable(code)||com.innerdimension.domain.runtime.DomainObjects.configurable(code);}
 public static boolean reverse(DomainSession s,String code){return s.objectMask(code)==2;}
 public static boolean valid(String code,int mask){return selectable(code)?mask==1||mask==2:mask>=0&&mask<=63&&(mask&~com.innerdimension.domain.runtime.DomainObjects.availableMask(code))==0;}
 public static boolean owned(java.util.Collection<String> species,String id){return species.contains(id);}
 public static int hearts(java.util.Collection<String> ids){boolean c=ids.contains("minecraft:chicken"),s=ids.contains(SUCCUBUS);return c&&s?2:c||s?1:0;}
 public static String migrateLegacySelection(String value){String[] parts=value.split("\\|",3);if(parts.length!=3)return value;var a=DomainAbilityCatalog.byEntity(parts[1]);return a!=null&&selectable(a.code())?parts[0]+"|"+parts[1]+"|1":value;}
 private DomainModes(){}
}
