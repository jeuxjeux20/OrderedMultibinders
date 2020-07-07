package com.github.jeuxjeux20.orderedmultibinders;

import com.github.jeuxjeux20.orderedmultibinders.internal.binding.IdentifiedAsOrderedBindingTransformer;

import java.lang.annotation.*;

/**
 * Specifies that bindings targeting this class will be identified using the
 * specified class.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@OrderedBindingAnnotation(IdentifiedAsOrderedBindingTransformer.class)
public @interface IdentifiedAs {
    /**
     * Returns the class this will be identified as.
     *
     * @return the class this will be identified as
     */
    Class<?> value();
}
