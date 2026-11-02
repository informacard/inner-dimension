package com.innerdimension.client;

import java.lang.reflect.*;
import net.minecraft.*;
import nonplayerplushie.SensoryLinkData;

/** New appearance helpers are optional; the original 1.5 model and skin APIs remain sufficient. */
public final class PlushieAppearanceCompat {
    private static Method optional(String type,String name,Class<?>... parameters){
        try{return Class.forName(type).getMethod(name,parameters);}
        catch(ClassNotFoundException|NoSuchMethodException absent){return null;}
    }
    private static final String APPEARANCE="nonplayerplushie.client.DollSkinAppearance";
    private static final Method DEFAULT=optional(APPEARANCE,"defaultLayer");
    private static final Method SKIN=optional(APPEARANCE,"resolveSkin",class_2487.class,class_11786.class);
    private static final Method LAYER=optional(APPEARANCE,"resolve",class_2487.class,class_11786.class_11787.class);
    private static final Method TEXTURES=optional(APPEARANCE,"textures",class_2487.class,class_11786.class_11787.class);
    private static final Method GENDER=optional("nonplayerplushie.client.DollGenderClient","genderMask",class_2487.class);
    private static final Method SHOW_PARTS=optional("nonplayerplushie.client.DollGenderClient","showParts",class_2487.class);
    private static final Method VARIANT=optional("nonplayerplushie.client.SensoryLinkDollModel","root",int.class);
    private static final Method MALE_VARIANT=optional("nonplayerplushie.client.SensoryLinkDollModel","root",boolean.class);
    private final nonplayerplushie.client.SensoryLinkDollModel wide=new nonplayerplushie.client.SensoryLinkDollModel();
    private final nonplayerplushie.client.SensoryLinkDollModel slim;
    public PlushieAppearanceCompat(){
        nonplayerplushie.client.SensoryLinkDollModel candidate=wide;
        try{candidate=nonplayerplushie.client.SensoryLinkDollModel.class.getConstructor(boolean.class).newInstance(true);}
        catch(NoSuchMethodException absent){}
        catch(ReflectiveOperationException failure){throw new IllegalStateException("Cannot create plushie model",failure);}
        slim=candidate;
    }
    private static Object call(Method method,Object target,Object... args){
        try{return method.invoke(target,args);}
        catch(ReflectiveOperationException failure){throw new IllegalStateException("Plushie appearance helper failed: "+method,failure);}
    }
    public static class_1921 defaultLayer(){return DEFAULT==null?class_11786.field_62215:(class_1921)call(DEFAULT,null);}
    public void appearance(class_2487 data,class_11786 provider,SensoryLinkDollRenderState state){
        state.layer=defaultLayer();state.slim=false;state.genderMask=GENDER!=null?(int)call(GENDER,null,data):SHOW_PARTS!=null&&(boolean)call(SHOW_PARTS,null,data)?1:0;
        var profile=SensoryLinkData.profile(data);if(profile==null)return;
        var skin=SKIN==null?provider.method_73495(profile):(class_11786.class_11787)call(SKIN,null,data,provider);
        var textures=TEXTURES==null?skin.method_73503():(class_8685)call(TEXTURES,null,data,skin);
        state.layer=LAYER==null?class_836.method_72165(textures.comp_1626().comp_3627()):(class_1921)call(LAYER,null,data,skin);
        state.slim="slim".equals(textures.comp_1629().method_15434());
    }
    public class_630 root(boolean narrow,int mask){
        var model=narrow?slim:wide;
        if(VARIANT!=null)return (class_630)call(VARIANT,model,mask);
        return MALE_VARIANT==null?model.root():(class_630)call(MALE_VARIANT,model,(mask&1)!=0);
    }
}
