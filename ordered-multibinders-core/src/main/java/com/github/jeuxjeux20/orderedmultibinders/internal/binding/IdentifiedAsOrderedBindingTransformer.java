package com.github.jeuxjeux20.orderedmultibinders.internal.binding;

import com.github.jeuxjeux20.orderedmultibinders.IdentifiedAs;
import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBinding;
import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBindingTransformer;

/**
 * Applies the @{@link IdentifiedAs} annotation.
 */
public class IdentifiedAsOrderedBindingTransformer implements OrderedBindingTransformer {
    @Override
    public OrderedBinding transform(OrderedBinding orderedBinding) {
        IdentifiedAs annotation = orderedBinding.getIdentifier().getRawType().getAnnotation(IdentifiedAs.class);

        if (annotation == null) {
            return orderedBinding;
        }

        return orderedBinding.change(its -> its.identifier(annotation.value()));
    }
}
