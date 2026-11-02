package com.innerdimension.domain;
import com.innerdimension.domain.runtime.DomainObjects;

/** Command digits run left to right in the same order as the object-selection screen. */
public final class DebugDomainRules {
    public static final int DELAY_TICKS=40;
    private static final int[] BITS={DomainObjects.SELF,DomainObjects.FRIENDLY,DomainObjects.NEUTRAL,DomainObjects.HOSTILE,DomainObjects.ACTIVE,DomainObjects.PASSIVE};
    private DebugDomainRules() {}
    public static int parseMask(String digits) {
        if(digits==null||!digits.matches("[01]{6}"))throw new IllegalArgumentException("对象参数必须是六位二进制数字，例如 111111 或 000000");
        int mask=0;for(int i=0;i<6;i++)if(digits.charAt(i)=='1')mask|=BITS[i];return mask;
    }
}
