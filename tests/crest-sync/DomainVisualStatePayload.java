package com.innerdimension.network;
import java.util.UUID;
public record DomainVisualStatePayload(UUID playerUuid, boolean active, int visualRevision, int domainAge, int burnoutRemaining) {
    public static final Object ID = new Object();
}
