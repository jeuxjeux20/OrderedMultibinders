package com.github.jeuxjeux20.orderedmultibinders;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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
    void inserted_after_when_last() {
        testItems(InsertedAfterWhenLast.EXPECTED_ITEMS, InsertedAfterWhenLast.TEST_ITEMS);
    }

    @Test
    void inserted_before_when_last() {
        testItems(InsertedBeforeWhenLast.EXPECTED_ITEMS, InsertedBeforeWhenLast.TEST_ITEMS);
    }

    @Test
    void last_item_after_when_first() {
        testItems(LastItemAfterWhenFirst.EXPECTED_ITEMS, LastItemAfterWhenFirst.TEST_ITEMS);
    }

    @Test
    void first_item_before_when_first() {
        testItems(FirstItemBeforeWhenFirst.EXPECTED_ITEMS, FirstItemBeforeWhenFirst.TEST_ITEMS);
    }

    @Test
    void circular_ordered_throws() {
        assertThrows(CycleDetectedException.class, () -> testItems(CircularReference.ITEMS));
    }

    @Test
    void self_referencing_ordered_throws() {
        assertThrows(CycleDetectedException.class, () -> testItems(SelfReference.ITEMS));
    }

    @Test
    void irresolvable_identifier_ordered_throws() {
        assertThrows(UnableToResolveClassAsBindingException.class, () -> testItems(IrresolvableIdentifier.ITEMS));
    }

    @Test
    void duplicate_identifiers_ordered_throws() {
        assertThrows(DuplicateIdentifiersException.class, () -> testItems(DuplicateIdentifiers.ITEMS));
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

    private void testItems(List<Object> expectedItems, List<Object> testItems) {
        Module module = new TestItemsModule(testItems);

        module = OrderedMultibinders.sort(module);
        Set<Object> set = resolveSet(module);

        assertIterableEquals(expectedItems, set);
    }

    private void testItems(List<Object> items) {
        testItems(items, items);
    }

    Set<Object> resolveSet(Module module) {
        Injector injector = Guice.createInjector(module);

        return injector.getInstance(Key.get(new TypeLiteral<Set<Object>>() {}));
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

    static class TestItemsModule extends AbstractModule {
        private final List<Object> items;

        TestItemsModule(List<Object> items) {
            this.items = items;
        }

        @Override
        protected void configure() {
            Multibinder<Object> multibinder = Multibinder.newSetBinder(binder(), Object.class);

            for (Object item : items) {
                multibinder.addBinding().toInstance(item);
            }
        }
    }

    static final class BackOrdered {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE, Last.INSTANCE);

        @Order(before = Second.class)
        enum First {INSTANCE}

        @Order(before = Last.class)
        enum Second {INSTANCE}

        enum Last {INSTANCE}
    }

    static final class FrontOrdered {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE, Last.INSTANCE);

        enum First {INSTANCE}

        @Order(after = First.class)
        enum Second {INSTANCE}

        @Order(after = Second.class)
        enum Last {INSTANCE}
    }

    static final class BothWaysOrdered {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE, Last.INSTANCE);

        @Order(before = Second.class)
        enum First {INSTANCE}

        @Order(after = First.class, before = Last.class)
        enum Second {INSTANCE}

        @Order(after = Second.class)
        enum Last {INSTANCE}
    }

    static final class InsertedAfterWhenLast {
        static ImmutableList<Object> TEST_ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE, PutMeInBetween.INSTANCE);
        static ImmutableList<Object> EXPECTED_ITEMS = ImmutableList.of(First.INSTANCE, PutMeInBetween.INSTANCE, Second.INSTANCE);

        enum First {INSTANCE}

        enum Second {INSTANCE}

        @Order(after = First.class)
        enum PutMeInBetween {INSTANCE}
    }

    static final class InsertedBeforeWhenLast {
        static ImmutableList<Object> TEST_ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE, PutMeInBetween.INSTANCE);
        static ImmutableList<Object> EXPECTED_ITEMS = ImmutableList.of(First.INSTANCE, PutMeInBetween.INSTANCE, Second.INSTANCE);

        enum First {INSTANCE}

        enum Second {INSTANCE}

        @Order(before = Second.class)
        enum PutMeInBetween {INSTANCE}
    }

    static final class LastItemAfterWhenFirst {
        static ImmutableList<Object> TEST_ITEMS = ImmutableList.of(Last.INSTANCE, First.INSTANCE, Second.INSTANCE);
        static ImmutableList<Object> EXPECTED_ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE, Last.INSTANCE);

        enum First {INSTANCE}

        enum Second {INSTANCE}

        @Order(after = First.class)
        enum Last {INSTANCE}
    }

    static final class FirstItemBeforeWhenFirst {
        static ImmutableList<Object> TEST_ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE, Last.INSTANCE);
        static ImmutableList<Object> EXPECTED_ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE, Last.INSTANCE);

        @Order(before = Last.class)
        enum First {INSTANCE}

        enum Second {INSTANCE}

        enum Last {INSTANCE}
    }

    // Exceptions

    static final class CircularReference {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE);

        @Order(after = Second.class)
        enum First {INSTANCE}

        @Order(after = First.class)
        enum Second {INSTANCE}
    }

    static final class SelfReference {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE);

        @Order(after = First.class)
        enum First {INSTANCE}
    }

    static final class IrresolvableIdentifier {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE);

        @Order(after = Irresolvable.class)
        enum First {INSTANCE}

        enum Irresolvable {}
    }

    static final class DuplicateIdentifiers {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE);

        @IdentifiedAs(First.class)
        enum First {INSTANCE}

        @IdentifiedAs(First.class)
        enum Second {INSTANCE}
    }
}
