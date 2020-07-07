package com.github.jeuxjeux20.orderedmultibinders;

import com.github.jeuxjeux20.orderedmultibinders.util.MultibinderFinder;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binding;
import com.google.inject.Module;
import com.google.inject.multibindings.*;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Sorts the elements of modules' {@link Multibinder}s using a {@link MultibinderSorter}.
 *
 * @see MultibinderSorter
 */
final class ModuleMultibinderSorter {
    private final ImmutableSet<Module> modules;

    ModuleMultibinderSorter(Iterable<? extends Module> modules) {
         this.modules = ImmutableSet.copyOf(modules);
    }

    public Module sort() {
        List<Element> allElements = new ArrayList<>(Elements.getElements(modules));
        MultibinderSorter multibinderSorter = new MultibinderSorter(allElements);

        for (MultibinderBinding<?> multibinder : MultibinderFinder.findMultibinders(allElements)) {
            List<Binding<?>> sortedBindings = multibinderSorter.sort(multibinder);

            // Remove them all and then re-add them so we get the correct order.
            allElements.removeAll(sortedBindings);
            allElements.addAll(sortedBindings);
        }

        return Elements.getModule(allElements);
    }
}
