package com.github.jeuxjeux20.orderedmultibinders;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.internal.ConstantBindingBuilderImpl;
import com.google.inject.internal.LinkedBindingImpl;
import com.google.inject.internal.Scoping;

public enum Placeholders {
    ;

    public static final Binding<?> BINDING =
            new LinkedBindingImpl<>("whatever", Key.get(Object.class), Scoping.UNSCOPED, Key.get(Object.class));

    public static class Cat {}

    public static class Dog {}
}
