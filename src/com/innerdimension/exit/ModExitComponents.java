package com.innerdimension.exit;

import com.innerdimension.InnerDimension;
import com.innerdimension.state.InnerState;
import net.minecraft.class_1799;
import net.minecraft.class_2378;
import net.minecraft.class_7923;
import net.minecraft.class_9331;

/**
 * The registered item component that remembers where an item entered an inner dimension.
 *
 * <p>It has to be a component rather than an entry in a world data file: an item that was fed in
 * through a dildo becomes a dropped item, gets picked up, may be split across two stacks or moved
 * into a funnel, and the entity that carried it stops existing. Storing the source on the
 * {@code ItemStack} means every one of those vanilla operations carries it along for free.</p>
 *
 * <p>Consequence, accepted by the specification: two stacks with different sources no longer
 * merge, because a component is part of stack equality. Splitting a stack copies the component to
 * both halves, which is the wanted behaviour - each half still knows its own entry.</p>
 */
public final class ModExitComponents {
    /**
     * Where this stack came in. Absent means "not an object that entered through a dildo", which
     * is also the normal state of everything produced inside a room.
     */
    public static final class_9331<InnerState.EntrySource> EXIT_SOURCE = register();

    private ModExitComponents() {
    }

    private static class_9331<InnerState.EntrySource> register() {
        class_9331.class_9332<InnerState.EntrySource> builder = class_9331.method_57873();
        return class_2378.method_10230(class_7923.field_49658, InnerDimension.id("exit_source"),
                builder.method_57881(InnerState.ENTRY_SOURCE_CODEC).method_57880());
    }

    /** Forces the static initialiser, i.e. the component registration, during mod init. */
    public static void init() {
        // Nothing to do: touching the class registers the component.
    }

    /** The recorded entry of a stack, or {@code null} when it has none. */
    public static InnerState.EntrySource sourceOf(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return null;
        }
        return stack.method_58694(EXIT_SOURCE);
    }

    /** Records where a stack entered. Ignored for an empty stack, which cannot hold components. */
    public static void attach(class_1799 stack, InnerState.EntrySource source) {
        if (stack == null || stack.method_7960() || source == null) {
            return;
        }
        stack.method_57379(EXIT_SOURCE, source);
    }

    /**
     * Drops the entry marker, used once an item has left the dimension.
     *
     * <p>Without this an item that came back out would still look like an inner object the next
     * time it is picked up, and it would refuse to stack with an identical plain stack.</p>
     */
    public static void clear(class_1799 stack) {
        if (stack != null && !stack.method_7960()) {
            stack.method_57379(EXIT_SOURCE, null);
        }
    }
}
