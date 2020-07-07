package com.github.jeuxjeux20.orderedmultibinders.util;

import com.google.inject.multibindings.MapBinderBinding;
import com.google.inject.multibindings.MultibinderBinding;
import com.google.inject.multibindings.MultibindingsTargetVisitor;
import com.google.inject.multibindings.OptionalBinderBinding;
import com.google.inject.spi.DefaultBindingTargetVisitor;

/**
 * Basically a visitor to get a {@link MultibinderBinding}.
 */
class MultibinderFilterVisitor
        extends DefaultBindingTargetVisitor<Object, MultibinderBinding<?>>
        implements MultibindingsTargetVisitor<Object, MultibinderBinding<?>> {

    @Override
    public MultibinderBinding<?> visit(MultibinderBinding<?> multibinding) {
        return multibinding;
    }

    @Override
    public MultibinderBinding<?> visit(MapBinderBinding<?> mapbinding) {
        return null;
    }

    @Override
    public MultibinderBinding<?> visit(OptionalBinderBinding<?> optionalbinding) {
        return null;
    }
}
