package com.github.jeuxjeux20.orderedmultibinders.config;

import com.github.jeuxjeux20.orderedmultibinders.Order;
import com.github.jeuxjeux20.orderedmultibinders.Placeholders;
import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBinding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static com.github.jeuxjeux20.orderedmultibinders.TestOrderedBindings.createOrderedBinding;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClosestDefaultPositionProviderTests {
    private static final DefaultPositionProvider DEFAULT_POSITION_PROVIDER
            = DefaultPositionProvider.CLOSEST;

    @Test
    void zero_with_no_order() {
        OrderedBinding orderedBinding = createOrderedBinding(NoOrder.class);

        int position = DEFAULT_POSITION_PROVIDER.get(orderedBinding);

        assertEquals(0, position);
    }

    @Test
    void zero_with_both_empty() {
        OrderedBinding orderedBinding = createOrderedBinding(BothEmpty.class);

        int position = DEFAULT_POSITION_PROVIDER.get(orderedBinding);

        assertEquals(0, position);
    }

    @Test
    void zero_with_both_non_empty() {
        OrderedBinding orderedBinding = createOrderedBinding(BothNonEmpty.class);

        int position = DEFAULT_POSITION_PROVIDER.get(orderedBinding);

        assertEquals(0, position);
    }

    @Test
    void positive_1_with_before_only() {
        OrderedBinding orderedBinding = createOrderedBinding(BeforeOnly.class);

        int position = DEFAULT_POSITION_PROVIDER.get(orderedBinding);

        assertEquals(1, position);
    }

    @Test
    void negative_1_with_after_only() {
        OrderedBinding orderedBinding = createOrderedBinding(AfterOnly.class);

        int position = DEFAULT_POSITION_PROVIDER.get(orderedBinding);

        assertEquals(-1, position);
    }


    static class NoOrder {}

    @Order
    static class BothEmpty {}

    @Order(before = Placeholders.Cat.class, after = Placeholders.Dog.class)
    static class BothNonEmpty {}

    @Order(before = Placeholders.Cat.class)
    static class BeforeOnly {}

    @Order(after = Placeholders.Dog.class)
    static class AfterOnly {}
}
