package com.github.jeuxjeux20.orderedmultibinders;

import com.github.jeuxjeux20.orderedmultibinders.internal.binding.GenericParameterOrderedBindingTransformer;
import org.jetbrains.annotations.Range;

import java.lang.annotation.*;

/**
 * Specifies that bindings targeting this class will be redirected using
 * a specific generic parameter.
 * <p>
 * The @{@link Order} annotation will be taken from the generic
 * parameter, and its annotations will be processed.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@OrderedBindingAnnotation(
        value = GenericParameterOrderedBindingTransformer.class,
        applyAnnotationsOfNewIdentifier = true
)
public @interface RedirectedByGenericParameter {
    /**
     * Returns the index of the generic parameter to use. This defaults to {@code 0} (the first one).
     *
     * @return the index of the generic parameter to use.
     */
    @Range(from = 0, to = Integer.MAX_VALUE) int value() default 0;
}
