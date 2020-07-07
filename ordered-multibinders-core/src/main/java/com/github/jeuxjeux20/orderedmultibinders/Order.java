package com.github.jeuxjeux20.orderedmultibinders;

import com.google.inject.multibindings.Multibinder;

import java.lang.annotation.*;

/**
 * Specifies the relative constraints on the position of bindings
 * targeting this class in a {@link Multibinder}.
 *
 * @see OrderedMultibinders
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
    /**
     * Returns the classes that this class should precede.
     * @return the classes that this class should precede
     */
    Class<?>[] before() default {};

    /**
     * Returns the classes that this class should succeed.
     * @return the classes that this class should succeed
     */
    Class<?>[] after() default {};

}
