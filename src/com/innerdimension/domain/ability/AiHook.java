package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

import java.util.List;

/**
 * AI participation. Abilities may refuse a target, refuse an active movement intent or
 * re-point a mob. Nothing here may set the NoAI flag or blindly teleport a mob.
 */
public interface AiHook {
    /** TRUE cancels the mob's choice of {@code target}; null abstains. */
    default Boolean denyTarget(DomainSession session, class_1308 mob, class_1309 target) { return null; }

    /** TRUE pauses this mob's active movement intent (D47). External velocity is untouched. */
    default boolean denyMove(DomainSession session, class_1308 mob) { return false; }

    /** Mobs this session pushes a mob towards, in priority order. */
    default List<class_1309> preferredTargets(DomainSession session, class_1308 mob) { return List.of(); }
}
