package com.github.jeuxjeux20.orderedmultibinders;

import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBinding;
import com.github.jeuxjeux20.orderedmultibinders.config.DefaultPositionProvider;
import com.github.jeuxjeux20.orderedmultibinders.config.SortingConfiguration;
import com.github.jeuxjeux20.orderedmultibinders.config.UnresolvableClassHandling;
import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
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
    void uses_multibinder_set_position_on_conflict() {
        testItems(PositionConflict.ITEMS);
    }

    @Test
    void before_first_with_negative_position() {
        testItems(BeforeFirstWithNegativePosition.EXPECTED_ITEMS,
                BeforeFirstWithNegativePosition.TEST_ITEMS);
    }

    @Test
    void before_in_between_with_positive_position() {
        testItems(BeforeInBetweenWithPositivePosition.EXPECTED_ITEMS,
                BeforeInBetweenWithPositivePosition.TEST_ITEMS);
    }

    @Test
    void after_in_between_with_negative_position() {
        testItems(AfterInBetweenWithNegativePosition.EXPECTED_ITEMS,
                AfterInBetweenWithNegativePosition.TEST_ITEMS);
    }

    @Test
    void after_last_with_positive_position() {
        testItems(AfterLastWithPositivePosition.EXPECTED_ITEMS,
                AfterLastWithPositivePosition.TEST_ITEMS);
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
    void irresolvable_identifier_ordered_throw_handling_throws() {
        SortingConfiguration config = SortingConfiguration.builder()
                .unresolvableClassHandling(UnresolvableClassHandling.THROW)
                .build();

        assertThrows(UnableToResolveClassAsBindingException.class,
                () -> testItems(IrresolvableIdentifier.ITEMS, config));
    }

    @Test
    void irresolvable_identifier_ordered_ignore_handling_ignores() {
        SortingConfiguration config = SortingConfiguration.builder()
                .unresolvableClassHandling(UnresolvableClassHandling.IGNORE)
                .build();

        testItems(IrresolvableIdentifier.ITEMS, config);
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

    private void testItems(List<Object> expectedItems, List<Object> testItems, SortingConfiguration configuration) {
        Module module = new TestItemsModule(testItems);

        module = OrderedMultibinders.sort(configuration, module);
        Set<Object> set = resolveSet(module);

        assertIterableEquals(expectedItems, set);
    }

    private void testItems(List<Object> expectedItems, List<Object> testItems) {
        testItems(expectedItems, testItems, SortingConfiguration.DEFAULT);
    }

    private void testItems(List<Object> items, SortingConfiguration configuration) {
        testItems(items, items, configuration);
    }

    private void testItems(List<Object> items) {
        testItems(items, SortingConfiguration.DEFAULT);
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

    static class PositionConflict {
        static ImmutableList<Object> ITEMS = ImmutableList.of(First.INSTANCE, Second.INSTANCE, Last.INSTANCE);

        enum First {INSTANCE}

        @Order(after = First.class)
        enum Second {INSTANCE}

        @Order(after = First.class)
        enum Last {INSTANCE}
    }

    static final class BeforeFirstWithNegativePosition {
        static ImmutableList<Object> TEST_ITEMS
                = ImmutableList.of(First.INSTANCE, Last.INSTANCE, PutMeFirst.INSTANCE);
        static ImmutableList<Object> EXPECTED_ITEMS
                = ImmutableList.of(PutMeFirst.INSTANCE, First.INSTANCE, Last.INSTANCE);

        enum First {INSTANCE}

        enum Last {INSTANCE}

        @Order(before = Last.class, position = -1)
        enum PutMeFirst {INSTANCE}
    }

    static final class BeforeInBetweenWithPositivePosition {
        static ImmutableList<Object> TEST_ITEMS
                = ImmutableList.of(First.INSTANCE, Last.INSTANCE, PutMeInBetween.INSTANCE);
        static ImmutableList<Object> EXPECTED_ITEMS
                = ImmutableList.of(First.INSTANCE, PutMeInBetween.INSTANCE, Last.INSTANCE);

        enum First {INSTANCE}

        enum Last {INSTANCE}

        @Order(before = Last.class, position = 1)
        enum PutMeInBetween {INSTANCE}
    }

    static final class AfterInBetweenWithNegativePosition {
        static ImmutableList<Object> TEST_ITEMS
                = ImmutableList.of(First.INSTANCE, Last.INSTANCE, PutMeInBetween.INSTANCE);
        static ImmutableList<Object> EXPECTED_ITEMS
                = ImmutableList.of(First.INSTANCE, PutMeInBetween.INSTANCE, Last.INSTANCE);

        enum First {INSTANCE}

        enum Last {INSTANCE}

        @Order(after = First.class, position = -1)
        enum PutMeInBetween {INSTANCE}
    }

    static final class AfterLastWithPositivePosition {
        static ImmutableList<Object> TEST_ITEMS
                = ImmutableList.of(First.INSTANCE, PutMeLast.INSTANCE, Last.INSTANCE);
        static ImmutableList<Object> EXPECTED_ITEMS
                = ImmutableList.of(First.INSTANCE, Last.INSTANCE, PutMeLast.INSTANCE);

        enum First {INSTANCE}

        enum Last {INSTANCE}

        @Order(after = Last.class, position = 1)
        enum PutMeLast {INSTANCE}
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
