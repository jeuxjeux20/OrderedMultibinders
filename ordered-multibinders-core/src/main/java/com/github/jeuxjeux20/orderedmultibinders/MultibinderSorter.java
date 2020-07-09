package com.github.jeuxjeux20.orderedmultibinders;

import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBinding;
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
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.*;

/**
 * Sorts a {@link Multibinder}'s elements according to their @{@link Order} annotation.
 */
final class MultibinderSorter {
    private final List<Element> elements;
    private final OrderedBindingFactory orderedBindingFactory = OrderedBindingFactory.DEFAULT;

    MultibinderSorter(List<Element> elements) {
        this.elements = elements;
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

        Comparator<Binding<?>> comparator = new BindingListPositionComparator(context);
        TopologicalOrderIterator<Binding<?>, BindingEdge> topologicalIterator =
                new TopologicalOrderIterator<>(context.graph, comparator);

        return ImmutableList.copyOf(topologicalIterator);
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
            return getIdentifier((Binding<?>) getSource()) + " -> " +
                   getIdentifier((Binding<?>) getTarget()) + (isExplicit ? " [explicit]" : " [implicit]");
        }

        private String getIdentifier(Binding<?> binding) {
            return context.orderedBindings.get(binding).getIdentifier().toString();
        }
    }

    /**
     * Creates the Directed Acyclic Graph (DAG) of a list of bindings.
     * <p>
     * Edges are created using the {@link Order} annotation contained in the binding's (<i>b</i>) class:
     * <ul>
     *     <li>Each binding (<i>a</i>) in {@link Order#before()} creates an edge such as <i>a -> b</i></li>
     *     <li>Each binding (<i>a</i>) in {@link Order#after()} creates an edge such as <i>b -> a</i></li>
     * </ul>
     */
    private static final class BindingGraphFactory {
        private final SortContext context;
        private final BindingListPositionComparator bindingListPositionComparator;

        BindingGraphFactory(SortContext context) {
            this.context = context;

            this.bindingListPositionComparator = new BindingListPositionComparator(context);
        }

        public BindingGraph createGraph() {
            BindingGraph graph = new BindingGraph(context);

            addVertexes(graph);
            createImplicitEdges(graph);
            createExplicitEdges(graph);
            flattenEdges(graph, FlatteningDirection.HEAD);
            flattenEdges(graph, FlatteningDirection.TAIL);

            return graph;
        }

        private void addVertexes(BindingGraph graph) {
            for (Binding<?> binding : context.bindings) {
                graph.addVertex(binding);
            }
        }

        private void createImplicitEdges(BindingGraph graph) {
            List<Binding<?>> bindings = context.bindings;

            for (int i = 0; i < bindings.size() - 1; i++) {
                Binding<?> binding = bindings.get(i);
                Binding<?> nextBinding = bindings.get(i + 1);

                if (isImplicitCandidate(binding) && isImplicitCandidate(nextBinding)) {
                    BindingEdge edge = graph.addEdge(binding, nextBinding);
                    edge.setExplicit(false);
                }
            }
        }

        private void createExplicitEdges(BindingGraph graph) {
            for (Binding<?> binding : context.bindings) {
                Order order = context.orderedBindings.get(binding).getOrder();
                if (order == null) {
                    continue;
                }

                for (Class<?> beforeClass : order.before()) {
                    addExplicitEdge(graph, binding, findByClassOrThrow(beforeClass));
                }

                for (Class<?> afterClass : order.after()) {
                    addExplicitEdge(graph, findByClassOrThrow(afterClass), binding);
                }
            }
        }

        private void flattenEdges(BindingGraph graph, FlatteningDirection direction) {
            for (Binding<?> binding : context.bindings) {
                List<BindingEdge> edges = new ArrayList<>(direction.getEdges(graph, binding));

                if (edges.size() > 1) {
                    // The ordering is determined by whoever comes first in the element list.
                    // The goal is to feel like you're using insert(item) to add stuff.

                    edges.sort((a, b) -> {
                        Binding<?> aEndpoint = direction.getEndpoint(graph, a);
                        Binding<?> bEndpoint = direction.getEndpoint(graph, b);

                        return bindingListPositionComparator.compare(bEndpoint, aEndpoint);
                    });

                    Binding<?> lastEndpoint = direction.getEndpoint(graph, edges.get(0));

                    // Ignore the first edge.
                    for (int i = 1; i < edges.size(); i++) {
                        BindingEdge edge = edges.get(i);
                        Binding<?> edgeEndpoint = direction.getEndpoint(graph, edge);

                        graph.removeEdge(edge);
                        direction.addEdge(graph, lastEndpoint, edgeEndpoint);

                        lastEndpoint = edgeEndpoint;
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

        private Binding<?> findByClassOrThrow(Class<?> clazz) {
            Binding<?> binding = context.orderedBindings.inverse().get(OrderedBinding.equalityToken(TypeLiteral.get(clazz)));

            if (binding != null) {
                return binding;
            } else {
                throw new UnableToResolveClassAsBindingException(clazz);
            }
        }

        private boolean isImplicitCandidate(Binding<?> binding) {
            OrderedBinding orderedBinding = context.orderedBindings.get(binding);

            if (orderedBinding == null) {
                return true;
            } else {
                Order order = orderedBinding.getOrder();
                return order == null || (order.before().length == 0 && order.after().length == 0);
            }
        }

        private CycleDetectedException cycleDetectedException(Binding<?> binding, Binding<?> otherBinding) {
            OrderedBinding orderedBinding = context.orderedBindings.get(binding);
            OrderedBinding otherOrderedBinding = context.orderedBindings.get(otherBinding);

            return new CycleDetectedException(
                    "Cycle detected between " + orderedBinding.getIdentifier() +
                    " and " + otherOrderedBinding.getIdentifier() + ".");
        }

        private enum FlatteningDirection {
            HEAD,
            TAIL;

            public <T, E> T getEndpoint(Graph<T, E> graph, E edge) {
                return this == HEAD ?
                        graph.getEdgeTarget(edge) :
                        graph.getEdgeSource(edge);
            }

            public <T, E> void addEdge(Graph<T, E> graph, T a, T b) {
                if (this == HEAD) {
                    graph.addEdge(a, b);
                } else {
                    graph.addEdge(b, a);
                }
            }

            public <T, E> Set<E> getEdges(Graph<T, E> graph, T vertex) {
                return this == HEAD ?
                        graph.outgoingEdgesOf(vertex) :
                        graph.incomingEdgesOf(vertex);
            }
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
}
