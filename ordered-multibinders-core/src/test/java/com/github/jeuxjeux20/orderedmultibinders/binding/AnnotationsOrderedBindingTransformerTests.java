package com.github.jeuxjeux20.orderedmultibinders.binding;

import com.github.jeuxjeux20.orderedmultibinders.IdentifiedAs;
import com.github.jeuxjeux20.orderedmultibinders.OrderedBindingAnnotation;
import com.github.jeuxjeux20.orderedmultibinders.Placeholders;
import com.github.jeuxjeux20.orderedmultibinders.internal.binding.AnnotationsOrderedBindingTransformer;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AnnotationsOrderedBindingTransformerTests extends OrderedBindingTransformerTestBase {
    static final OrderedBindingTransformer TRANSFORMER = new AnnotationsOrderedBindingTransformer();

    @BeforeEach
    void before() {

    }

    @Test
    void processes_single_annotation() {
        testTransformer(TRANSFORMER,
                createOrderedBinding(IWantToBeACat.class),
                createOrderedBinding(Placeholders.Cat.class));
    }

    @Test
    void processes_multiple_annotations() {
        testTransformer(TRANSFORMER,
                createOrderedBinding(IWantToBeAListOfCats.class),
                createOrderedBinding(new TypeLiteral<List<Placeholders.Cat>>(){}));
    }

    @Test
    void applies_annotations_on_new_identifier_when_asked_to() {
        testTransformer(TRANSFORMER,
                createOrderedBinding(IWantToBeACatButIndirectly.class),
                createOrderedBinding(Placeholders.Cat.class));
    }

    @Test
    void uninstantiable_processor_throws() {
        OrderedBinding binding = createOrderedBinding(UninstantiableProcessorThing.class);

        assertThrows(IllegalArgumentException.class, () -> TRANSFORMER.transform(binding));
    }

    @Catify
    static class IWantToBeACat {}

    @Catify
    @Listify
    static class IWantToBeAListOfCats {}

    @ApplyUninstantiableProcessor
    static class UninstantiableProcessorThing {}

    @ChangeToWantBeACat
    static class IWantToBeACatButIndirectly {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @OrderedBindingAnnotation(Catify.Processor.class)
    @interface Catify {
        class Processor implements OrderedBindingTransformer {
            @Override
            public OrderedBinding transform(OrderedBinding orderedBinding) {
                return orderedBinding.change(its -> its.identifier(Placeholders.Cat.class));
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @OrderedBindingAnnotation(Listify.Processor.class)
    @interface Listify {
        class Processor implements OrderedBindingTransformer {
            @Override
            public OrderedBinding transform(OrderedBinding orderedBinding) {
                TypeLiteral<?> newIdentifier = TypeLiteral.get(Types.listOf(orderedBinding.getIdentifier().getType()));

                return orderedBinding.change(its -> its.identifier(newIdentifier));
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @OrderedBindingAnnotation(ApplyUninstantiableProcessor.Processor.class)
    @interface ApplyUninstantiableProcessor {
        class Processor implements OrderedBindingTransformer {
            Processor(String blah) {}

            @Override
            public OrderedBinding transform(OrderedBinding orderedBinding) {
                return orderedBinding;
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @OrderedBindingAnnotation(value = Catify.Processor.class, applyAnnotationsOfNewIdentifier = true)
    @interface ChangeToWantBeACat {
        class Processor implements OrderedBindingTransformer {
            @Override
            public OrderedBinding transform(OrderedBinding orderedBinding) {
                return orderedBinding.change(its -> its.identifier(IWantToBeACat.class));
            }
        }
    }
}
