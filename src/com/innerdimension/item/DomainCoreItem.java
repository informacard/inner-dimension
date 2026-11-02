package com.innerdimension.item;
import net.minecraft.*;
import nonplayerplushie.SensoryLinkData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
public final class DomainCoreItem extends class_1792 {
    public DomainCoreItem(class_1793 settings) { super(settings); }
    @Override public void method_67187(class_1799 stack,class_9635 context,class_10712 tooltipContext,java.util.function.Consumer<class_2561> lines,class_1836 type) {
        super.method_67187(stack,context,tooltipContext,lines,type);
        String owner=SensoryLinkData.ownerName(SensoryLinkData.read(stack));
        lines.accept(owner==null ? class_2561.method_43471("item.inner_dimension.domain_core.unbound") : class_2561.method_43469("item.inner_dimension.domain_core.bound",owner));
        lines.accept(class_2561.method_43471("item.inner_dimension.domain_core.soulbound"));
    }
    public class_1269 method_7836(class_1937 world,class_1657 user,class_1268 hand) {
        if(!user.method_5667().equals(SensoryLinkData.ownerUuid(SensoryLinkData.read(user.method_5998(hand))))) {
            if(user instanceof class_3222 p) p.method_7353(class_2561.method_43471("inner_dimension.core.not_owner"),true);
            return class_1269.field_5814;
        }
        if(user instanceof class_3222 p) ServerPlayNetworking.send(p,OpenDomainCorePayload.forPlayer(p));
        return class_1269.field_5812;
    }
}
