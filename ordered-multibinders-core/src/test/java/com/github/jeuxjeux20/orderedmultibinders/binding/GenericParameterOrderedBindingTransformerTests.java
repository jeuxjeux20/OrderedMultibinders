package com.github.jeuxjeux20.orderedmultibinders.binding;

import com.github.jeuxjeux20.orderedmultibinders.Order;
import com.github.jeuxjeux20.orderedmultibinders.Placeholders;
import com.github.jeuxjeux20.orderedmultibinders.RedirectedByGenericParameter;
import com.github.jeuxjeux20.orderedmultibinders.internal.binding.GenericParameterOrderedBindingTransformer;
import com.google.inject.TypeLiteral;
import org.junit.jupiter.api.Test;

import static com.github.jeuxjeux20.orderedmultibinders.TestOrderedBindings.createOrderedBinding;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GenericParameterOrderedBindingTransformerTests extends OrderedBindingTransformerTestBase {
    static final OrderedBindingTransformer TRANSFORMER = new GenericParameterOrderedBindingTransformer();

    static final Order ARGUMENT_ANNOTATION = Argument.class.getAnnotation(Order.class);

    @Test
    void redirects_to_generic_argument() {
        testTransformer(TRANSFORMER,
                createOrderedBinding(new TypeLiteral<GenericType<Argument>>() {}),
                createOrderedBinding(Argument.class, ARGUMENT_ANNOTATION));
    }

    @Test
    void invalid_generic_parameter_throws() {
        OrderedBinding binding = createOrderedBinding(new TypeLiteral<InvalidGenericParameter<Argument>>() {});

        assertThrows(IllegalArgumentException.class, () -> TRANSFORMER.transform(binding));
    }

    @Test
    void non_parametrized_type_throws() {
        OrderedBinding binding = createOrderedBinding(NoGenericParameter.class);

        assertThrows(IllegalArgumentException.class, () -> TRANSFORMER.transform(binding));
    }

    @Test
    void does_not_affect_when_annotation_absent() {
        testTransformerSame(TRANSFORMER, createOrderedBinding(Placeholders.Cat.class));
    }

    @RedirectedByGenericParameter
    static class GenericType<T> {}

    @RedirectedByGenericParameter(value = 1)
    static class InvalidGenericParameter<T> {}

    @RedirectedByGenericParameter
    static class NoGenericParameter {}

    @Order(before = Placeholders.Cat.class)
    static class Argument {}
}
