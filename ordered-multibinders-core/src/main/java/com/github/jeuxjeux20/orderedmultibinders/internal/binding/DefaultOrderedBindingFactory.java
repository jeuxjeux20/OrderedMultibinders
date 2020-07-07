package com.github.jeuxjeux20.orderedmultibinders.internal.binding;

import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBinding;
import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBindingTransformer;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binding;
import com.google.inject.TypeLiteral;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class DefaultOrderedBindingFactory implements OrderedBindingFactory {
    private final BindingTargetTypeFinder bindingTargetTypeFinder;
    private final ImmutableList<OrderedBindingTransformer> orderedBindingTransformers;

    DefaultOrderedBindingFactory(BindingTargetTypeFinder bindingTargetTypeFinder,
                                 List<OrderedBindingTransformer> orderedBindingTransformers) {
        this.bindingTargetTypeFinder = bindingTargetTypeFinder;
        this.orderedBindingTransformers = ImmutableList.copyOf(orderedBindingTransformers);
    }

    @Override
    public @Nullable OrderedBinding create(Binding<?> binding) {
        TypeLiteral<?> bindingTargetType = bindingTargetTypeFinder.findTargetType(binding);

        if (bindingTargetType == null) {
            return null;
        }

        OrderedBinding orderedBinding = OrderedBinding.fromType(bindingTargetType, binding);

        orderedBinding = applyAllTransformers(orderedBinding);

        return orderedBinding;
    }

    private OrderedBinding applyAllTransformers(OrderedBinding orderedBinding) {
        for (OrderedBindingTransformer orderedBindingTransformer : orderedBindingTransformers) {
            orderedBinding = orderedBindingTransformer.transform(orderedBinding);
        }
        return orderedBinding;
    }
}
