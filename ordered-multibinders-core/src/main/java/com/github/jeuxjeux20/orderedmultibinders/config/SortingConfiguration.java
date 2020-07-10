package com.github.jeuxjeux20.orderedmultibinders.config;

import com.github.jeuxjeux20.orderedmultibinders.Order;
import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBinding;

import java.util.Objects;

/**
 * Defines the configuration for sorting elements.
 */
public final class SortingConfiguration {
    public static final DefaultPositionProvider DEFAULT_DEFAULT_POSITION
            = DefaultPositionProvider.CLOSEST;

    public static final UnresolvableClassHandling DEFAULT_UNRESOLVABLE_CLASS_HANDLING =
            UnresolvableClassHandling.THROW;

    /**
     * The default configuration. Default values are specified on every getter.
     */
    public static final SortingConfiguration DEFAULT = new SortingConfiguration();

    private final DefaultPositionProvider defaultPosition;
    private final UnresolvableClassHandling unresolvableClassHandling;

    private SortingConfiguration() {
        this(DEFAULT_DEFAULT_POSITION, DEFAULT_UNRESOLVABLE_CLASS_HANDLING);
    }

    private SortingConfiguration(DefaultPositionProvider defaultPosition,
                                 UnresolvableClassHandling unresolvableClassHandling) {
        this.defaultPosition = defaultPosition;
        this.unresolvableClassHandling = unresolvableClassHandling;
    }

    /**
     * Creates a new builder with the default values.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder with the values from the given configuration
     *
     * @param configuration the configuration to copy the values from
     * @return a new builder
     */
    public static Builder builder(SortingConfiguration configuration) {
        return new Builder(configuration);
    }

    /**
     * Gets the default position, which can be used on a specific {@link OrderedBinding}.
     * <p>
     * The default value is {@link DefaultPositionProvider#CLOSEST}.
     *
     * @return the default position provider
     */
    public DefaultPositionProvider getDefaultPosition() {
        return defaultPosition;
    }

    /**
     * Gets the behavior when a class in @{@link Order} can't be resolved as a binding.
     * <p>
     * The default value is {@link UnresolvableClassHandling#THROW}.
     *
     * @return the handling for an unresolvable class
     */
    public UnresolvableClassHandling getUnresolvableClassHandling() {
        return unresolvableClassHandling;
    }

    public static class Builder {
        private DefaultPositionProvider defaultPositionProvider;
        private UnresolvableClassHandling unresolvableClassHandling;

        public Builder() {
            this(DEFAULT);
        }

        public Builder(SortingConfiguration configuration) {
            this.defaultPositionProvider = configuration.defaultPosition;
            this.unresolvableClassHandling = configuration.unresolvableClassHandling;
        }

        /**
         * Sets the default position, used when an element has no @{@link Order}
         * annotation or when its {@link Order#position()} value is 0.
         *
         * @param defaultPositionProvider the default position
         * @return the same builder
         * @throws NullPointerException when the given value is null
         */
        public Builder defaultPosition(DefaultPositionProvider defaultPositionProvider) {
            this.defaultPositionProvider = Objects.requireNonNull(defaultPositionProvider);
            return this;
        }

        /**
         * Sets how unresolvable classes should be handled.
         *
         * @param unresolvableClassHandling the handling for unresolvable classes
         * @return the same builder
         * @throws NullPointerException when the given value is null
         */
        public Builder unresolvableClassHandling(UnresolvableClassHandling unresolvableClassHandling) {
            this.unresolvableClassHandling = Objects.requireNonNull(unresolvableClassHandling);
            return this;
        }

        /**
         * Builds a configuration using the values this builder has.
         *
         * @return a {@link SortingConfiguration} with the values of this builder
         */
        public SortingConfiguration build() {
            return new SortingConfiguration(defaultPositionProvider, unresolvableClassHandling);
        }
    }
}
