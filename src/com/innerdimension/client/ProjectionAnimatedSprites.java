package com.innerdimension.client;

import com.innerdimension.mixin.ProjectionBlockRenderDispatcherAccessor;
import com.innerdimension.mixin.ProjectionFluidRendererAccessor;
import net.minecraft.*;
import java.lang.reflect.Method;
import java.util.*;

/** Sodium only uploads animated atlas frames that a chunk renderer marks active. Projection
 * meshes bypass chunks, so keep their exact model/fluid sprites active while they are visible. */
final class ProjectionAnimatedSprites {
    private static final Object SODIUM;
    private static final Method HAS_ANIMATION, MARK_ACTIVE;
    static {
        Object api=null; Method animated=null,mark=null;
        try {
            Class<?> type=Class.forName("net.caffeinemc.mods.sodium.api.texture.SpriteUtil");
            api=type.getField("INSTANCE").get(null);
            animated=type.getMethod("hasAnimation",class_1058.class);
            mark=type.getMethod("markSpriteActive",class_1058.class);
        } catch (ReflectiveOperationException ignored) { }
        SODIUM=api; HAS_ANIMATION=animated; MARK_ACTIVE=mark;
    }
    private ProjectionAnimatedSprites() { }

    static Set<class_1058> collectStates(Iterable<class_2680> states) {
        Set<class_1058> sprites=Collections.newSetFromMap(new IdentityHashMap<>());
        class_776 dispatcher=class_310.method_1551().method_1541();
        class_5819 random=class_5819.method_43053();
        Set<class_2680> visited=Collections.newSetFromMap(new IdentityHashMap<>());
        for(class_2680 state:states) {
            if(!visited.add(state))continue;
            class_1087 model=dispatcher.method_3349(state);
            List<class_10889> parts=new ArrayList<>();
            random.method_43052(0);
            model.method_68513(random,parts);
            for(class_10889 part:parts) {
                sprites.add(part.comp_3752());
                addQuads(sprites,part,null);
                for(class_2350 side:class_2350.values()) addQuads(sprites,part,side);
            }
        }
        var fluids=(ProjectionFluidRendererAccessor)(Object)
                ((ProjectionBlockRenderDispatcherAccessor)(Object)dispatcher).inner$fluidRenderer();
        sprites.add(fluids.inner$lavaStill()); sprites.add(fluids.inner$lavaFlowing());
        sprites.add(fluids.inner$waterStill()); sprites.add(fluids.inner$waterFlowing());
        sprites.add(fluids.inner$waterOverlay());
        sprites.remove(null);
        return sprites;
    }

    private static void addQuads(Set<class_1058> sprites,class_10889 part,class_2350 side) {
        for(class_777 quad:part.method_68509(side)) sprites.add(quad.comp_3724());
    }

    static void mark(Set<class_1058> sprites) {
        if(SODIUM==null) return;
        try {
            for(class_1058 sprite:sprites) if((boolean)HAS_ANIMATION.invoke(SODIUM,sprite))
                MARK_ACTIVE.invoke(SODIUM,sprite);
        } catch (ReflectiveOperationException ignored) { }
    }
}
