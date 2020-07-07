package com.github.jeuxjeux20.orderedmultibinders.demo;

import com.github.jeuxjeux20.orderedmultibinders.RedirectedByGenericParameter;
import com.github.jeuxjeux20.orderedmultibinders.Order;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Types;

import java.util.EnumSet;

public class FactoriesDemoRunner extends DemoRunner {
    public FactoriesDemoRunner() {
        this(EnumSet.of(Detail.CONTENTS));
    }

    public FactoriesDemoRunner(EnumSet<Detail> detail) {
        super("Factories", detail, new TestModule());
    }

    public static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            Multibinder<Thing.Factory<?>> multibinder = Multibinder.newSetBinder(binder(),
                    new TypeLiteral<Thing.Factory<?>>() {});

            multibinder.addBinding().to(Thing.Factory.typeOf(HighThing.class));
            multibinder.addBinding().to(Thing.Factory.typeOf(SuperiorThing.class));
            multibinder.addBinding().to(Thing.Factory.typeOf(LowThing.class));
            multibinder.addBinding().to(Thing.Factory.typeOf(MediumThing.class));
        }
    }

    public interface Thing {
        @RedirectedByGenericParameter
        class Factory<T extends Thing> {
            // Let's assume it's has methods to actually create stuff ;)
            private final TypeLiteral<T> type;

            @Inject
            Factory(TypeLiteral<T> type) {
                this.type = type;
            }

            @Override
            public String toString() {
                return "I create instances of " + type;
            }

            @SuppressWarnings("unchecked")
            public static <T extends Thing> TypeLiteral<Factory<T>> typeOf(Class<T> clazz) {
                return (TypeLiteral<Factory<T>>) TypeLiteral.get(
                        Types.newParameterizedTypeWithOwner(Thing.class, Factory.class, clazz)
                );
            }
        }
    }

    @Order(before = MediumThing.class)
    public static class LowThing implements Thing {
    }

    @Order(before = HighThing.class)
    public static class MediumThing implements Thing {
    }
    
    public static class HighThing implements Thing {
    }

    @Order(after = HighThing.class)
    public static class SuperiorThing implements Thing {
    }
}
