package com.github.jeuxjeux20.orderedmultibinders.demo;

import com.github.jeuxjeux20.orderedmultibinders.Order;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import java.util.EnumSet;

public class BakeACakeDemo extends DemoRunner {
    public BakeACakeDemo() {
        this(EnumSet.of(Detail.CONTENTS));
    }

    public BakeACakeDemo(EnumSet<Detail> detail) {
        super("Bake a cake", detail, new BaseCakeModule(), new SprinklesCakeModule());
    }

    abstract static class RecipeOperation {
        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    static final class BaseCakeModule extends AbstractModule {
        @Override
        protected void configure() {
            Multibinder<RecipeOperation> operations = Multibinder.newSetBinder(binder(), RecipeOperation.class);

            operations.addBinding().to(PreheatOven.class);
            operations.addBinding().to(AddIngredients.class);
            operations.addBinding().to(BakeInOven.class);
            operations.addBinding().to(WaitUntilItIsColdEnough.class);
        }
    }
    
    static class PreheatOven extends RecipeOperation {}

    static class AddIngredients extends RecipeOperation {}

    static class BakeInOven extends RecipeOperation {}

    static class WaitUntilItIsColdEnough extends RecipeOperation {}

    static final class SprinklesCakeModule extends AbstractModule {
        @Override
        protected void configure() {
            Multibinder<RecipeOperation> operations = Multibinder.newSetBinder(binder(), RecipeOperation.class);

            operations.addBinding().to(AddSprinkles.class);
        }
    }

    @Order(after = BakeInOven.class)
    static class AddSprinkles extends RecipeOperation {}
}
