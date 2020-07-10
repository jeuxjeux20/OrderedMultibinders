package com.github.jeuxjeux20.orderedmultibinders.internal.binding;

import com.github.jeuxjeux20.orderedmultibinders.Order;
import com.github.jeuxjeux20.orderedmultibinders.RedirectedByGenericParameter;
import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBinding;
import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBindingTransformer;
import com.google.inject.TypeLiteral;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Applies the @{@link RedirectedByGenericParameter} annotation.
 */
public class GenericParameterOrderedBindingTransformer implements OrderedBindingTransformer {
    private static IllegalArgumentException cannotFindGenericArgument(Type actualType, int argumentIndex) {
        return new IllegalArgumentException(
                "Cannot find generic argument at index " + argumentIndex + " on identifier " + actualType);
    }

    @Override
    public OrderedBinding transform(OrderedBinding orderedBinding) {
        TypeLiteral<?> type = orderedBinding.getIdentifier();

        RedirectedByGenericParameter annotation
                = type.getRawType().getAnnotation(RedirectedByGenericParameter.class);

        if (annotation == null) {
            return orderedBinding;
        }

        Type actualType = type.getType();
        int argumentIndex = annotation.value();

        if (actualType instanceof ParameterizedType) {
            Type[] genericArguments = ((ParameterizedType) actualType).getActualTypeArguments();

            if (genericArguments.length <= argumentIndex) {
                throw cannotFindGenericArgument(actualType, argumentIndex);
            } else {
                Type genericArgument = genericArguments[argumentIndex];
                TypeLiteral<?> genericArgumentType = TypeLiteral.get(genericArgument);

                Order newOrder = genericArgumentType.getRawType().getAnnotation(Order.class);

                return orderedBinding.change(its -> its.identifier(genericArgumentType).order(newOrder));
            }
        } else {
            throw cannotFindGenericArgument(actualType, argumentIndex);
        }
    }
}
