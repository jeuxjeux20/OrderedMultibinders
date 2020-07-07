package com.github.jeuxjeux20.orderedmultibinders;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class OrderedMultibinderTests {
    @Test
    void back_ordered() {
        testItems(BackOrdered.ITEMS);
    }

    @Test
    void front_ordered() {
        testItems(FrontOrdered.ITEMS);
    }

    @Test
    void both_ways_ordered() {
        testItems(BothWaysOrdered.ITEMS);
    }

    @Test
    void circular_ordered_throws() {
        assertThrows(CycleDetectedException.class, () -> testItems(CircularOrdered.ITEMS));
    }

    @Test
    void self_referencing_ordered_throws() {
        assertThrows(CycleDetectedException.class, () -> testItems(SelfReferencingOrdered.ITEMS));
    }

    @Test
    void irresolvable_identifier_ordered_throws() {
        assertThrows(UnableToResolveClassAsBindingException.class, () -> testItems(IrresolvableIdentifierOrdered.ITEMS));
    }

    @Test
    void duplicate_identifiers_ordered_throws() {
        assertThrows(DuplicateIdentifiersException.class, () -> testItems(DuplicateIdentifiersOrdered.ITEMS));
    }

    @Test
    void same_item_with_1() {
        Module oldModule = new OneItemModule();
        Set<Object> oldSet = resolveSet(oldModule);

        Module newModule = OrderedMultibinders.sort(oldModule);
        Set<Object> newSet = resolveSet(newModule);

        assertIterableEquals(oldSet, newSet);
    }

    @Test
    void no_items_with_0() {
        Module module = OrderedMultibinders.sort(new NoItemsModule());

        Set<Object> set = resolveSet(module);

        assertTrue(set.isEmpty());
    }

    private void testItems(List<Object> items) {
        Module module = TestItemsModule.fromItems(items);

        module = OrderedMultibinders.sort(module);
        Set<Object> set = resolveSet(module);

        assertIterableEquals(items, set);
    }

    Set<Object> resolveSet(Module module) {
        Injector injector = Guice.createInjector(module);

        return injector.getInstance(Key.get(new TypeLiteral<Set<Object>>(){}));
    }

    static final class OneItemModule extends AbstractModule {
        @Override
        protected void configure() {
            Multibinder.newSetBinder(binder(), Object.class).addBinding().toInstance(4);
        }
    }

    static final class NoItemsModule extends AbstractModule {
        @Override
        protected void configure() {
            Multibinder.newSetBinder(binder(), Object.class);
        }
    }

    static abstract class TestItemsModule extends AbstractModule {
        public static TestItemsModule fromItems(List<Object> items) {
            return new TestItemsModule() {
                @Override
                protected List<Object> getItems() {
                    return items;
                }
            };
        }


        @Override
        protected void configure() {
            Multibinder<Object> multibinder = Multibinder.newSetBinder(binder(), Object.class);

            List<Object> items = new ArrayList<>(getItems());
            Collections.shuffle(items);

            for (Object item : items) {
                multibinder.addBinding().toInstance(item);
            }
        }

        protected abstract List<Object> getItems();
    }

    static final class BackOrdered {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE, Last.INSTANCE);

        @Order(before = Second.class)
        enum First { INSTANCE }

        @Order(before = Last.class)
        enum Second { INSTANCE }

        enum Last { INSTANCE }
    }

    static final class FrontOrdered {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE, Last.INSTANCE);

        enum First { INSTANCE }

        @Order(after = First.class)
        enum Second { INSTANCE }

        @Order(after = Second.class)
        enum Last { INSTANCE }
    }

    static final class BothWaysOrdered {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE, Last.INSTANCE);

        @Order(before = Second.class)
        enum First { INSTANCE }

        @Order(after = First.class, before = Last.class)
        enum Second { INSTANCE }

        @Order(after = Second.class)
        enum Last { INSTANCE }
    }

    static final class CircularOrdered {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE);

        @Order(after = Second.class)
        enum First { INSTANCE }

        @Order(after = First.class)
        enum Second { INSTANCE }
    }

    static final class SelfReferencingOrdered {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE);

        @Order(after = First.class)
        enum First { INSTANCE }
    }

    static final class IrresolvableIdentifierOrdered {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE);

        @Order(after = Irresolvable.class)
        enum First { INSTANCE }

        enum Irresolvable {}
    }

    static final class DuplicateIdentifiersOrdered {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE);

        @IdentifiedAs(First.class)
        enum First { INSTANCE }

        @IdentifiedAs(First.class)
        enum Second { INSTANCE }
    }
}
