package com.github.jeuxjeux20.orderedmultibinders.binding;

import com.github.jeuxjeux20.orderedmultibinders.IdentifiedAs;
import com.github.jeuxjeux20.orderedmultibinders.Order;
import com.github.jeuxjeux20.orderedmultibinders.Placeholders;
import com.github.jeuxjeux20.orderedmultibinders.internal.binding.IdentifiedAsOrderedBindingTransformer;
import org.junit.jupiter.api.Test;

public class IdentifiedAsOrderedBindingTransformerTests extends OrderedBindingTransformerTestBase {
    static final OrderedBindingTransformer TRANSFORMER = new IdentifiedAsOrderedBindingTransformer();

    static final Order IDENTIFY_ANNOTATION = IdentifyMe.class.getAnnotation(Order.class);

    @Test
    void places_new_identifier() {
        testTransformer(TRANSFORMER,
                createOrderedBinding(IdentifyMe.class),
                createOrderedBinding(NewIdentifier.class, IDENTIFY_ANNOTATION));
    }

    @Test
    void does_not_affect_when_annotation_absent() {
        testTransformerSame(TRANSFORMER, createOrderedBinding(Placeholders.Cat.class));
    }

    @Order(before = Placeholders.Dog.class)
    static class NewIdentifier {}

    @IdentifiedAs(NewIdentifier.class)
    @Order(before = Placeholders.Cat.class)
    static class IdentifyMe {}
}
