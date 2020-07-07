package com.github.jeuxjeux20.orderedmultibinders.internal.binding;

import com.github.jeuxjeux20.orderedmultibinders.OrderedBindingAnnotation;
import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBinding;
import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBindingTransformer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

/**
 * Applies all annotations annotated with @{@link OrderedBindingAnnotation}.
 * <p>
 * If the ordered binding's identifier changed through the process,
 * all annotations on this new identifier will also be applied, and so on.
 */
public class AnnotationsOrderedBindingTransformer implements OrderedBindingTransformer {
    @Override
    public OrderedBinding transform(OrderedBinding orderedBinding) {
        for (Annotation annotation : orderedBinding.getIdentifier().getRawType().getAnnotations()) {
            OrderedBindingAnnotation orderedBindingAnnotation =
                    annotation.annotationType().getAnnotation(OrderedBindingAnnotation.class);

            if (orderedBindingAnnotation == null) {
                continue;
            }

            OrderedBinding initialOrderedBinding = orderedBinding;

            OrderedBindingTransformer orderedBindingTransformer = instantiateIdentifierProcessor(orderedBindingAnnotation.value());
            orderedBinding = orderedBindingTransformer.transform(orderedBinding);

            // If the binding's identifier changed, make sure
            // that annotations present on that new identifier are also used.
            if (orderedBindingAnnotation.applyAnnotationsOfNewIdentifier() &&
                !initialOrderedBinding.getIdentifier().equals(orderedBinding.getIdentifier())) {
                orderedBinding = this.transform(orderedBinding);
            }
        }

        return orderedBinding;
    }

    private OrderedBindingTransformer instantiateIdentifierProcessor(Class<? extends OrderedBindingTransformer> clazz) {
        try {
            Constructor<? extends OrderedBindingTransformer> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't instantiate identifier processor " + clazz + ".", e);
        }
    }
}
