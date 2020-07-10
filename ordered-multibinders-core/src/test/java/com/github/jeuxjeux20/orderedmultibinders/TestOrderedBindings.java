package com.github.jeuxjeux20.orderedmultibinders;

import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBinding;
import com.google.inject.TypeLiteral;

public final class TestOrderedBindings {
    private TestOrderedBindings() {
    }

    public static OrderedBinding createOrderedBinding(TypeLiteral<?> type) {
        return OrderedBinding.fromType(type, Placeholders.BINDING);
    }

    public static OrderedBinding createOrderedBinding(TypeLiteral<?> type, Order order) {
        return new OrderedBinding(type, Placeholders.BINDING, order);
    }

    public static OrderedBinding createOrderedBinding(Class<?> clazz) {
        return OrderedBinding.fromType(clazz, Placeholders.BINDING);
    }

    public static OrderedBinding createOrderedBinding(Class<?> clazz, Order order) {
        return new OrderedBinding(clazz, Placeholders.BINDING, order);
    }
}
