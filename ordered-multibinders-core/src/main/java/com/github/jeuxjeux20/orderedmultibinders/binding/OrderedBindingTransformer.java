package com.github.jeuxjeux20.orderedmultibinders.binding;

import com.github.jeuxjeux20.orderedmultibinders.IdentifiedAs;
import com.github.jeuxjeux20.orderedmultibinders.OrderedBindingAnnotation;
import com.github.jeuxjeux20.orderedmultibinders.internal.binding.AnnotationsOrderedBindingTransformer;
import com.google.inject.TypeLiteral;

/**
 * A function that transforms a {@link OrderedBinding} into another one, with different values,
 * or not.
 * <p>
 * For example, the @{@link IdentifiedAs} annotation applies an {@link OrderedBindingTransformer}
 * that changes transforms the identifier into the annotation value.
 *
 * @see OrderedBindingAnnotation
 */
@FunctionalInterface
public interface OrderedBindingTransformer {
    /**
     * The {@link OrderedBindingTransformer} that applies annotations annotated with @{@link OrderedBindingAnnotation}.
     */
    OrderedBindingTransformer ANNOTATIONS = new AnnotationsOrderedBindingTransformer();

    /**
     * Transforms the given {@link OrderedBinding} into another one, which may also be the
     * same as the initial value.
     *
     * @param orderedBinding the {@link OrderedBinding} to transform
     * @return the transformed {@link OrderedBinding}
     */
    OrderedBinding transform(OrderedBinding orderedBinding);
}
