package com.github.jeuxjeux20.orderedmultibinders.util;

import com.google.inject.Binding;
import com.google.inject.multibindings.MultibinderBinding;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple helper to find multibinders and their bindings.
 */
public final class MultibinderFinder {
    private MultibinderFinder() {
    }

    /**
     * Finds all multibinders from the specified elements.
     *
     * @param elements the elements where should be found the multibinders
     * @return the found {@link MultibinderBinding}s
     */
    public static List<MultibinderBinding<?>> findMultibinders(List<? extends Element> elements) {
        // For some reason javac 8 needs this cast.

        return elements.stream()
                .map(element -> (MultibinderBinding<?>) element.acceptVisitor(new DefaultElementVisitor<MultibinderBinding<?>>() {
                    @Override
                    public <B> MultibinderBinding<?> visit(Binding<B> binding) {
                        return binding.acceptTargetVisitor(new MultibinderFilterVisitor());
                    }
                }))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Finds all the bindings that the specified multibinder contains in the specified list
     * of elements.
     *
     * @param elements    the list elements containing the multibinder bindings
     * @param multibinder the multibinder
     * @return a list of bindings that the specified multibinder contains
     */
    public static List<Binding<?>> findMultibinderBindings(List<? extends Element> elements,
                                                           MultibinderBinding<?> multibinder) {
        return getBaseMultibinderElementsStream(elements, multibinder)
                .collect(Collectors.toList());
    }

    /**
     * Finds all the bindings that the specified multibinder contains in the specified list
     * of elements, except the set key and the alternate set keys.
     *
     * @param elements    the list elements containing the multibinder bindings
     * @param multibinder the multibinder
     * @return a list of bindings that the specified multibinder contains,
     * except the set key and the alternate set keys
     */
    public static List<Binding<?>> findMultibinderContentBindings(List<? extends Element> elements,
                                                                  MultibinderBinding<?> multibinder) {
        return getBaseMultibinderElementsStream(elements, multibinder)
                .filter(e -> !multibinder.getSetKey().equals(e.getKey()) &&
                             !multibinder.getAlternateSetKeys().contains(e.getKey()))
                .collect(Collectors.toList());
    }

    private static Stream<? extends Binding<?>> getBaseMultibinderElementsStream(List<? extends Element> elements,
                                                                                 MultibinderBinding<?> multibinder) {
        return elements.stream()
                .filter(multibinder::containsElement)
                .map(element -> element instanceof Binding<?> ? ((Binding<?>) element) : null)
                .filter(Objects::nonNull);
    }
}
