package com.github.jeuxjeux20.orderedmultibinders;

import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBinding;
import com.github.jeuxjeux20.orderedmultibinders.config.SortingConfiguration;
import com.github.jeuxjeux20.orderedmultibinders.config.UnresolvableClassHandling;
import com.github.jeuxjeux20.orderedmultibinders.internal.binding.OrderedBindingBiMapFactory;
import com.github.jeuxjeux20.orderedmultibinders.internal.binding.OrderedBindingFactory;
import com.github.jeuxjeux20.orderedmultibinders.util.MultibinderFinder;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binding;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.MultibinderBinding;
import com.google.inject.spi.Element;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sorts a {@link Multibinder}'s elements according to their @{@link Order} annotation.
 */
final class MultibinderSorter {
    private final List<Element> elements;
    private final OrderedBindingFactory orderedBindingFactory = OrderedBindingFactory.DEFAULT;
    private final SortingConfiguration configuration;

    MultibinderSorter(List<Element> elements, SortingConfiguration configuration) {
        this.elements = elements;
        this.configuration = configuration;
    }

    public List<Binding<?>> sort(MultibinderBinding<?> multibinder) {
        List<Binding<?>> multiBinderElements = MultibinderFinder.findMultibinderContentBindings(elements, multibinder);

        if (multiBinderElements.isEmpty()) {
            return multiBinderElements;
        }

        SortContext context = createSortContext(multiBinderElements);

        BindingGraphFactory graphFactory = new BindingGraphFactory(context);
        context.graph = graphFactory.createGraph();

        return sortElements(context);
    }

    private MultibinderSorter.SortContext createSortContext(List<Binding<?>> bindings) {
        ImmutableBiMap<Binding<?>, OrderedBinding> orderedBindings =
                new OrderedBindingBiMapFactory(orderedBindingFactory).createOrderedBindings(bindings);

        return new SortContext(bindings, orderedBindings);
    }

    private List<Binding<?>> sortElements(SortContext context) {
        if (context.bindings.size() < 2) {
            // There is no ordering to apply with under two elements.
            return context.bindings;
        }

        Comparator<Binding<?>> listPositionComparator = new BindingListPositionComparator(context);
        TopologicalOrderIterator<Binding<?>, BindingEdge> topologicalIterator =
                new TopologicalOrderIterator<>(context.graph, (a, b) -> {
                    int aPosition = findPosition(context, a);
                    int bPosition = findPosition(context, b);

                    int positionComparison = Integer.compare(aPosition, bPosition);
                    if (positionComparison != 0) {
                        return positionComparison;
                    } else {
                        return listPositionComparator.compare(a, b);
                    }
                });

        return ImmutableList.copyOf(topologicalIterator);
    }

    private int findPosition(SortContext context, Binding<?> binding) {
        OrderedBinding orderedBinding = context.orderedBindings.get(binding);
        Order order = orderedBinding.getOrder();

        int position = 0;

        if (order != null) {
            position = order.position();
        }

        if (position == 0) {
            position = configuration.getDefaultPosition().get(orderedBinding);
        }
        return position;

    }

    private static final class BindingGraph extends DirectedAcyclicGraph<Binding<?>, BindingEdge> {
        BindingGraph(SortContext context) {
            super(null, () -> new BindingEdge(context), false);
        }
    }

    private static final class BindingEdge extends DefaultEdge {
        private final SortContext context;

        private boolean isExplicit = true;

        public BindingEdge(SortContext context) {
            this.context = context;
        }

        public boolean isExplicit() {
            return isExplicit;
        }

        public void setExplicit(boolean explicit) {
            isExplicit = explicit;
        }

        @Override
        public String toString() {
            return getIdentifier((Binding<?>) getSource()) + " " +
                   getIdentifier((Binding<?>) getTarget()) + (isExplicit ? " [explicit]" : " [implicit]");
        }

        private String getIdentifier(Binding<?> binding) {
            return context.orderedBindings.get(binding).getIdentifier().getRawType().getSimpleName();
        }
    }

    private static final class BindingListPositionComparator implements Comparator<Binding<?>> {
        private final SortContext context;

        public BindingListPositionComparator(SortContext context) {
            this.context = context;
        }

        @Override
        public int compare(Binding<?> a, Binding<?> b) {
            return context.bindingPositions.get(a).compareTo(context.bindingPositions.get(b));
        }
    }

    private static final class SortContext {
        final List<Binding<?>> bindings;
        final BiMap<Binding<?>, OrderedBinding> orderedBindings;
        final Map<Binding<?>, Integer> bindingPositions;
        BindingGraph graph;

        private SortContext(List<Binding<?>> bindings, BiMap<Binding<?>, OrderedBinding> orderedBindings) {
            this.bindings = bindings;
            this.orderedBindings = orderedBindings;

            this.bindingPositions = createBindingPositions(bindings);
        }

        private Map<Binding<?>, Integer> createBindingPositions(List<Binding<?>> bindings) {
            Map<Binding<?>, Integer> map = new HashMap<>();
            for (int i = 0; i < bindings.size(); i++) {
                map.put(bindings.get(i), i);
            }

            return map;
        }
    }

    private final class BindingGraphFactory {
        private final SortContext context;

        BindingGraphFactory(SortContext context) {
            this.context = context;
        }

        public BindingGraph createGraph() {
            BindingGraph graph = new BindingGraph(context);

            addVertexes(graph);
            createImplicitEdges(graph);
            createExplicitEdges(graph);

            return graph;
        }

        private void addVertexes(BindingGraph graph) {
            for (Binding<?> binding : context.bindings) {
                graph.addVertex(binding);
            }
        }

        private void createImplicitEdges(BindingGraph graph) {
            List<Binding<?>> bindings = context.bindings;

            Binding<?> lastImplicitBinding = null;
            for (Binding<?> binding : bindings) {
                if (isImplicitCandidate(binding)) {
                    if (lastImplicitBinding != null) {
                        BindingEdge edge = graph.addEdge(lastImplicitBinding, binding);
                        edge.setExplicit(false);
                    }

                    lastImplicitBinding = binding;
                }
            }
        }

        private boolean isImplicitCandidate(Binding<?> binding) {
            OrderedBinding orderedBinding = context.orderedBindings.get(binding);
            Order order = orderedBinding.getOrder();

            return order == null ||
                   (order.before().length == 0 && order.after().length == 0 && order.position() == 0);
        }

        private void createExplicitEdges(BindingGraph graph) {
            for (Binding<?> binding : context.bindings) {
                Order order = context.orderedBindings.get(binding).getOrder();
                if (order == null) {
                    continue;
                }

                for (Class<?> beforeClass : order.before()) {
                    Binding<?> succeedingBinding = findByClassOrHandle(beforeClass);
                    if (succeedingBinding != null) {
                        addExplicitEdge(graph, binding, succeedingBinding);
                    }
                }

                for (Class<?> afterClass : order.after()) {
                    Binding<?> precedingBinding = findByClassOrHandle(afterClass);
                    if (precedingBinding != null) {
                        addExplicitEdge(graph, precedingBinding, binding);
                    }
                }
            }
        }

        private void addExplicitEdge(BindingGraph graph, Binding<?> binding, Binding<?> succeedingBinding) {
            try {
                graph.addEdge(binding, succeedingBinding);
            } catch (IllegalArgumentException e) {
                throw cycleDetectedException(binding, succeedingBinding);
            }
        }

        private @Nullable Binding<?> findByClassOrHandle(Class<?> clazz) {
            Binding<?> binding = context.orderedBindings.inverse().get(OrderedBinding.equalityToken(TypeLiteral.get(clazz)));

            if (binding != null) {
                return binding;
            } else {
                UnresolvableClassHandling handling = configuration.getUnresolvableClassHandling();

                switch (handling) {
                    case THROW:
                        throw new UnableToResolveClassAsBindingException(clazz);
                    case IGNORE:
                        return null;
                    default:
                        throw new UnsupportedOperationException("Unknown handling: " + handling);
                }
            }
        }

        private CycleDetectedException cycleDetectedException(Binding<?> binding, Binding<?> otherBinding) {
            OrderedBinding orderedBinding = context.orderedBindings.get(binding);
            OrderedBinding otherOrderedBinding = context.orderedBindings.get(otherBinding);

            return new CycleDetectedException(
                    "Cycle detected between " + orderedBinding.getIdentifier() +
                    " and " + otherOrderedBinding.getIdentifier() + ".");
        }
    }
}
