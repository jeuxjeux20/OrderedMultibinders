package com.github.jeuxjeux20.orderedmultibinders.demo;

import com.github.jeuxjeux20.orderedmultibinders.IdentifiedAs;
import com.github.jeuxjeux20.orderedmultibinders.Order;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import java.util.EnumSet;

public class BakeACakeDemo extends DemoRunner {
    public BakeACakeDemo() {
        this(EnumSet.of(Detail.CONTENTS));
    }

    public BakeACakeDemo(EnumSet<Detail> detail) {
        super("Bake a cake", detail, new BasicCakeModule(), new SprinklesCakeModule());
    }

    abstract static class RecipeStep {
        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    static final class BasicCakeModule extends AbstractModule {
        @Override
        protected void configure() {
            Multibinder<RecipeStep> steps = Multibinder.newSetBinder(binder(), RecipeStep.class);

            steps.addBinding().to(PreheatOven.class);
            steps.addBinding().to(AddIngredients.class);
            steps.addBinding().to(BakeInOven.class);
            steps.addBinding().to(WaitUntilItIsColdEnough.class);
        }
    }

    static class PreheatOven extends RecipeStep {}

    static class AddIngredients extends RecipeStep {}

    static class BakeInOven extends RecipeStep {}

    static class WaitUntilItIsColdEnough extends RecipeStep {}

    static final class SprinklesCakeModule extends AbstractModule {
        @Override
        protected void configure() {
            Multibinder<RecipeStep> operations = Multibinder.newSetBinder(binder(), RecipeStep.class);

            operations.addBinding().to(AddSprinkles.class);
        }
    }

    @Order(after = BakeInOven.class)
    static class AddSprinkles extends RecipeStep {}
}
