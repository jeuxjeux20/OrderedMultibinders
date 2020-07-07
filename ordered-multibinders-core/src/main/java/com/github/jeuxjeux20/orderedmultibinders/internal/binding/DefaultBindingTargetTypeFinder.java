package com.github.jeuxjeux20.orderedmultibinders.internal.binding;

import com.google.inject.Binding;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.*;
import org.jetbrains.annotations.Nullable;

class DefaultBindingTargetTypeFinder
        extends DefaultBindingTargetVisitor<Object, TypeLiteral<?>>
        implements BindingTargetTypeFinder {
    @Override
    public TypeLiteral<?> visit(UntargettedBinding<?> untargettedBinding) {
        return untargettedBinding.getKey().getTypeLiteral();
    }

    @Override
    public TypeLiteral<?> visit(InstanceBinding<?> instanceBinding) {
        return TypeLiteral.get(instanceBinding.getInstance().getClass());
    }

    @Override
    public TypeLiteral<?> visit(ConstructorBinding<?> constructorBinding) {
        return constructorBinding.getConstructor().getDeclaringType();
    }

    @Override
    public TypeLiteral<?> visit(ConvertedConstantBinding<?> convertedConstantBinding) {
        return TypeLiteral.get(convertedConstantBinding.getValue().getClass());
    }

    @Override
    public TypeLiteral<?> visit(ProviderKeyBinding<?> providerKeyBinding) {
        return providerKeyBinding.getProviderKey().getTypeLiteral();
    }

    @Override
    public TypeLiteral<?> visit(ProviderInstanceBinding<?> providerInstanceBinding) {
        return TypeLiteral.get(providerInstanceBinding.getUserSuppliedProvider().getClass());
    }

    @Override
    public TypeLiteral<?> visit(LinkedKeyBinding<?> linkedKeyBinding) {
        return linkedKeyBinding.getLinkedKey().getTypeLiteral();
    }

    @Override
    public @Nullable TypeLiteral<?> findTargetType(Binding<?> binding) {
        return binding.acceptTargetVisitor(this);
    }

    // Can't implement exposed binding.
}
