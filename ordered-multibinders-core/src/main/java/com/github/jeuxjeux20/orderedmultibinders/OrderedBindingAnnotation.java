package com.github.jeuxjeux20.orderedmultibinders;

import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBindingTransformer;

import java.lang.annotation.*;

/**
 * Specifies that the annotation is an identifier annotation that applies a {@link OrderedBindingTransformer}
 * on classes where this annotation is present.
 * <p>
 * It is recommended that identifier annotations are annotated with @{@link Documented}.
 *
 * @see OrderedBindingTransformer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface OrderedBindingAnnotation {
    /**
     * Returns a {@link OrderedBindingTransformer} class <b>with a parameter-less constructor or
     * a default constructor</b> instantiated and applied to
     * classes where this annotation is present.
     *
     * @return a {@link OrderedBindingTransformer} class used on classes where this annotation is present
     */
    Class<? extends OrderedBindingTransformer> value();

    /**
     * Returns whether or not annotations on an identifier different than the one before
     * the transformation should be applies.
     *
     * @return {@code true} when annotations are applied on new identifiers, or {@code false} when not
     */
    boolean applyAnnotationsOfNewIdentifier() default false;
}
