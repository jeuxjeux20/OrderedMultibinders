package com.github.jeuxjeux20.orderedmultibinders.binding;

import com.github.jeuxjeux20.orderedmultibinders.Order;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.LinkedBindingImpl;
import com.google.inject.internal.Scoping;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Represents an ordered binding with an identifier, its actual binding and its @{@link Order} annotation.
 * <p>
 * This is an immutable class, and a builder is available to create a new instance
 * or create a new object based off another instance's values
 * (using {@link #builder(OrderedBinding)} or {@link #change(Consumer)}).
 * <p>
 * Two ordered bindings are considered equal if their identifier is the same (see {@link #equals(Object)}).
 */
public final class OrderedBinding {
    private static final Binding<?> NULL_BINDING
            = new LinkedBindingImpl<>("OrderedBinding_NULL", Key.get(Void.class), Scoping.UNSCOPED, Key.get(Void.class));

    private final Binding<?> binding;
    private final TypeLiteral<?> identifier;
    private final @Nullable Order order;

    /**
     * Constructs a new {@link OrderedBinding} instance with
     * the specified identifier, binding, and order (the latter may be {@code null}).
     *
     * @param identifier the identifier, as a class
     * @param binding    the binding
     * @param order      the order of the binding, which may be {@code null}
     */
    public OrderedBinding(Class<?> identifier, Binding<?> binding, @Nullable Order order) {
        this(TypeLiteral.get(requireNonNull(identifier, "identifier is null")), binding, order);
    }

    /**
     * Constructs a new {@link OrderedBinding} instance with
     * the specified identifier, binding, and order (the latter may be {@code null}).
     *
     * @param identifier the identifier
     * @param binding    the binding
     * @param order      the order of the binding, which may be {@code null}
     */
    public OrderedBinding(TypeLiteral<?> identifier, Binding<?> binding, @Nullable Order order) {
        this.identifier = requireNonNull(identifier, "identifier is null");
        this.binding = requireNonNull(binding, "binding is null");
        this.order = order;
    }

    /**
     * Creates a new builder with the specified identifier, binding, and @{@link Order} annotation
     * retrieved from the identifier.
     *
     * @param identifier the identifier
     * @param binding    the binding
     * @return a builder with the specified identifier and binding
     */
    public static Builder builder(TypeLiteral<?> identifier, Binding<?> binding) {
        return new Builder(identifier, binding);
    }

    /**
     * Creates a new builder with the specified identifier, binding, and @{@link Order} annotation
     * retrieved from the identifier.
     *
     * @param clazz   the class, used as an identifier
     * @param binding the binding
     * @return a builder with the specified identifier, binding, and the identifier's @{@link Order} annotation
     */
    public static Builder builder(Class<?> clazz, Binding<?> binding) {
        return new Builder(TypeLiteral.get(clazz), binding);
    }

    /**
     * Creates a new builder with all the values of the specified ordered binding.
     *
     * @param orderedBinding the ordered binding to copy the values from
     * @return a builder with the values of the specified ordered binding
     */
    public static Builder builder(OrderedBinding orderedBinding) {
        return new Builder(orderedBinding);
    }

    /**
     * Creates a new ordered binding using the specified binding and identifier,
     * and the identifier's @{@link Order} annotation,
     * if there isn't any, the order will be {@code null}.
     *
     * @param identifier the identifier, also used to retrieve the @{@link Order} annotation.
     * @param binding    the binding
     * @return a ordered binding with the specified identifier, and, if present, the identifier's @{@link Order} annotation
     */
    public static OrderedBinding fromType(TypeLiteral<?> identifier, Binding<?> binding) {
        return new OrderedBinding(identifier, binding, identifier.getRawType().getAnnotation(Order.class));
    }

    /**
     * Creates a new ordered binding using the specified class, which will
     * be wrapped using {@link TypeLiteral#get(Class)}, and the class's @{@link Order} annotation,
     * if there isn't any, the order will be {@code null}.
     *
     * @param clazz the class used to identify the binding
     * @param binding the actual binding
     * @return a ordered binding with the specified class, and, if present, the class' @{@link Order} annotation
     */
    public static OrderedBinding fromType(Class<?> clazz, Binding<?> binding) {
        return fromType(TypeLiteral.get(clazz), binding);
    }

    /**
     * Creates an equality token, which should only be used in scenarios such as {@link Map#get(Object)},
     * or even {@link Set#contains(Object)}.
     *
     * @param identifier the identifier
     * @return an equality token {@link OrderedBinding}, with the specified
     * identifier, a dummy binding and a null order
     */
    public static OrderedBinding equalityToken(TypeLiteral<?> identifier) {
        return new OrderedBinding(identifier, NULL_BINDING, null);
    }

    /**
     * Gets the identifier that identifies a binding.
     * <p>
     * This value is used, for example, to match bindings
     * with their class values in {@link Order}.
     *
     * @return the identifier that identifies a binding
     */
    public TypeLiteral<?> getIdentifier() {
        return identifier;
    }

    /**
     * Gets the @{@link Order} annotation of this ordered binding, which may be {@code null}.
     *
     * @return the @{@link Order} annotation of this ordered binding, which may be {@code null}.
     */
    public @Nullable Order getOrder() {
        return order;
    }

    /**
     * Gets the actual binding.
     * @return the actual binding
     */
    public Binding<?> getBinding() {
        return binding;
    }

    /**
     * Creates a new builder based off this ordered binding, then runs the specified
     * consumer with the builder created earlier, and returns
     * the result of the builder.
     * <p>
     * <b>Example: </b>
     * <pre>OrderedBinding newBinding = orderedBinding.change(its -&gt; its.identifier(someType));</pre>
     *
     * @param builderConsumer the consumer which will be run to apply modifications to the builder
     * @return the result of the builder
     */
    public OrderedBinding change(Consumer<? super Builder> builderConsumer) {
        Builder builder = builder(this);

        builderConsumer.accept(builder);

        return builder.build();
    }

    /**
     * Returns {@code true} if the specified object meets these requirements:
     * <ul>
     *     <li>it is an instance of {@link OrderedBinding}</li>
     *     <li>both instances have an equal identifier</li>
     * </ul>
     *
     * @param o the object to test for equality
     * @return {@code true} if the objects are considered equal, otherwise {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderedBinding that = (OrderedBinding) o;
        return Objects.equal(identifier, that.identifier);
    }

    /**
     * Returns the hash code of the identifier.
     *
     * @return the hash code of the identifier
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("identifier", identifier)
                .add("order", order)
                .add("binding", binding)
                .toString();
    }

    /**
     * The builder for {@link OrderedBinding}.
     */
    public static class Builder {
        private TypeLiteral<?> type;
        private Binding<?> binding;
        private @Nullable Order order;

        /**
         * Creates a new {@link Builder} with the specified identifier and binding, and
         * retrieves the @{@link Order} annotation from the given identifier.
         *
         * @param identifier the identifier that identifies a binding
         * @param binding the actual binding
         */
        public Builder(TypeLiteral<?> identifier, Binding<?> binding) {
            this(OrderedBinding.fromType(identifier, binding));
        }

        /**
         * Creates a new {@link Builder} with all properties copied
         * from the specified ordered binding.
         *
         * @param orderedBinding the ordered binding to copy the properties from
         */
        public Builder(OrderedBinding orderedBinding) {
            this.type = orderedBinding.identifier;
            this.binding = orderedBinding.binding;
            this.order = orderedBinding.order;
        }

        /**
         * Sets the identifier to the specified class, wrapped using {@link TypeLiteral#get(Class)}.
         *
         * @param clazz the class to set as a identifier
         * @return the same builder
         */
        @CanIgnoreReturnValue
        public Builder identifier(Class<?> clazz) {
            this.type = TypeLiteral.get(requireNonNull(clazz, "clazz is null"));
            return this;
        }

        /**
         * Sets the identifier to the specified one.
         *
         * @param type the identifier
         * @return the same builder
         */
        @CanIgnoreReturnValue
        public Builder identifier(TypeLiteral<?> type) {
            this.type = requireNonNull(type, "identifier is null");
            return this;
        }

        /**
         * Sets the binding to the specified one.
         *
         * @param binding the binding
         * @return the same builder
         */
        @CanIgnoreReturnValue
        public Builder binding(Binding<?> binding) {
            this.binding = requireNonNull(binding, "binding is null");
            return this;
        }

        /**
         * Sets the order to the specified one, it may be {@code null}.
         *
         * @param order the order, which may be {@code null}
         * @return the same builder
         */
        @CanIgnoreReturnValue
        public Builder order(@Nullable Order order) {
            this.order = order;
            return this;
        }

        /**
         * Creates an instance of {@link OrderedBinding} using the values of this builder.
         *
         * @return an instance of {@link OrderedBinding} with the values of this builder
         */
        public OrderedBinding build() {
            return new OrderedBinding(type, binding, order);
        }
    }
}
