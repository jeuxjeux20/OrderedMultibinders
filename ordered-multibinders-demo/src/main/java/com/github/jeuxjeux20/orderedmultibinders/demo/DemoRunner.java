package com.github.jeuxjeux20.orderedmultibinders.demo;

import com.github.jeuxjeux20.orderedmultibinders.OrderedMultibinders;
import com.github.jeuxjeux20.orderedmultibinders.util.MultibinderFinder;
import com.google.inject.Module;
import com.google.inject.*;
import com.google.inject.multibindings.MultibinderBinding;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class DemoRunner {
    public static final EnumSet<Detail> DEFAULT_DETAIL = EnumSet.of(Detail.CONTENTS);

    private final String name;
    private final Module[] modules;
    private final EnumSet<Detail> detail;

    public DemoRunner(String name, EnumSet<Detail> detail, Module... modules) {
        this.name = name;
        this.detail = detail;
        this.modules = modules;
    }

    public DemoRunner(String name, Module... modules) {
        this(name, DEFAULT_DETAIL, modules);
    }

    public final void run() {
        if (detail.contains(Detail.BINDINGS)) {
            printMultibindersBindings("UNSORTED", modules);
        }

        Module sortedModule = OrderedMultibinders.sort(modules);
        if (detail.contains(Detail.BINDINGS)) {
            printMultibindersBindings("SORTED", sortedModule);
        }

        if (detail.contains(Detail.CONTENTS)) {
            Injector injector = Guice.createInjector(sortedModule);
            printMultibinderContents(injector);
        }
    }

    @SuppressWarnings("unchecked")
    private void printMultibinderContents(Injector injector) {
        List<? extends MultibinderBinding<?>> multibinders = MultibinderFinder.findMultibinders(injector.getElements());
        for (MultibinderBinding<?> multibinder : multibinders) {
            printInfo("Multibinder contents: " + multibinder.getSetKey());

            // Safe because... well... it's literally a Set key.
            // RANT: Why doesn't it return a Key<Set<?>> ?????
            Set<?> multibinderContents = injector.getInstance((Key<Set<?>>) multibinder.getSetKey());

            multibinderContents.forEach(System.out::println);
        }
    }

    private void printMultibindersBindings(String prefix, Module... modules) {
        List<Element> elements = Elements.getElements(modules);
        for (MultibinderBinding<?> multibinder : MultibinderFinder.findMultibinders(elements)) {
            printInfo(prefix + " multibinder content bindings: " + multibinder.getSetKey());

            List<Binding<?>> multibinderContentBindings
                    = MultibinderFinder.findMultibinderContentBindings(elements, multibinder);

            multibinderContentBindings.forEach(System.out::println);
        }
        System.out.println();
    }

    private void printInfo(String string) {
        System.out.println("[" + name + "]: " + string);
    }

    public enum Detail {
        CONTENTS,
        BINDINGS
    }
}
