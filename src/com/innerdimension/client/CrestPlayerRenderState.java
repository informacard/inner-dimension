package com.innerdimension.client;

import java.util.UUID;

/** Identity copied into a queued player render state by the client mixin. */
public interface CrestPlayerRenderState {
    UUID inner$crestPlayer();
    void inner$crestPlayer(UUID id);
}
