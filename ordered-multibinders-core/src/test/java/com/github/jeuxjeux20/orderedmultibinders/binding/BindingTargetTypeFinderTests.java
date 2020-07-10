package com.github.jeuxjeux20.orderedmultibinders.binding;

import com.github.jeuxjeux20.orderedmultibinders.Placeholders;
import com.github.jeuxjeux20.orderedmultibinders.internal.binding.BindingTargetTypeFinder;
import com.google.inject.Binding;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.Elements;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BindingTargetTypeFinderTests {
    static final BindingTargetTypeFinder BINDING_TYPE_FINDER = BindingTargetTypeFinder.DEFAULT;

    @Test
    void untargetted_returns_key_type() {
        testBindingType(
                binder -> binder.bind(Placeholders.Cat.class),
                Placeholders.Cat.class
        );
    }

    @Test
    void instance_returns_instance_class() {
        testBindingType(
                binder -> binder.bind(Object.class).toInstance(4),
                Integer.class
        );
    }

    @Test
    void constructor_returns_enclosing_type() {
        testBindingType(
                binder -> binder.bind(Object.class).toConstructor(ClassWithSpecialConstructor.CONSTRUCTOR),
                ClassWithSpecialConstructor.class
        );
    }

    @Test
    void provider_key_returns_key_type() {
        testBindingType(
                binder -> binder.bind(Object.class).toProvider(SomeProvider.class),
                SomeProvider.class
        );
    }

    @Test
    void provider_instance_returns_instance_class() {
        testBindingType(
                binder -> binder.bind(Object.class).toProvider(new SomeProvider()),
                SomeProvider.class
        );
    }

    @Test
    void linked_key_returns_key_type() {
        testBindingType(
                binder -> binder.bind(Object.class).to(Placeholders.Cat.class),
                Placeholders.Cat.class
        );
    }

    private void testBindingType(Module module, Class<?> expected) {
        testBindingType(module, TypeLiteral.get(expected));
    }

    private void testBindingType(Module module, TypeLiteral<?> expected) {
        Binding<?> binding = Elements.getElements(module).stream()
                .filter(Binding.class::isInstance)
                .map(x -> (Binding<?>) x)
                // .filter(x -> x.getKey().getAnnotationType() == TestTarget.class)
                .findFirst().orElse(null);
        Assumptions.assumeFalse(binding == null, "There isn't any bindings in the given module.");

        TypeLiteral<?> result = BINDING_TYPE_FINDER.findTargetType(binding);

        assertEquals(expected, result);
    }

    static class ClassWithSpecialConstructor {
        static Constructor<ClassWithSpecialConstructor> CONSTRUCTOR;

        static {
            try {
                CONSTRUCTOR = ClassWithSpecialConstructor.class.getConstructor(int.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        public ClassWithSpecialConstructor(int thing) {
        }
    }

    static class SomeProvider implements Provider<Object> {
        @Override
        public Object get() {
            return "whatever";
        }
    }
}
