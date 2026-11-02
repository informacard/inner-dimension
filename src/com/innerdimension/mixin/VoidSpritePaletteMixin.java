package com.innerdimension.mixin;
import com.innerdimension.client.VoidTexturePalette;
import java.util.List;
import java.util.Optional;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_7764.class)
public class VoidSpritePaletteMixin {
    @Inject(method="<init>(Lnet/minecraft/class_2960;Lnet/minecraft/class_7771;Lnet/minecraft/class_1011;Ljava/util/Optional;Ljava/util/List;Ljava/util/Optional;)V",at=@At("RETURN"))
    private void inner$voidPalette(class_2960 id,class_7771 dimensions,class_1011 image,Optional<class_1079> animation,List<class_7677.class_11554<?>> metadata,Optional<class_1084> scaling,CallbackInfo ci){
        if(!id.equals(class_2960.method_60655("inner_dimension","block/void_water"))&&!id.equals(class_2960.method_60655("inner_dimension","block/void_mist")))return;
        int width=image.method_4307(),height=image.method_4323();int[] pixels=new int[width*height];
        for(int y=0;y<height;y++)for(int x=0;x<width;x++)pixels[y*width+x]=image.method_61940(x,y);
        VoidTexturePalette.apply(pixels);
        for(int y=0;y<height;y++)for(int x=0;x<width;x++)image.method_61941(x,y,pixels[y*width+x]);
        org.slf4j.LoggerFactory.getLogger("inner_dimension").info("Void palette loaded: {} ({}x{})",id,width,height);
    }
}
