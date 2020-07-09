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

        Comparator<Binding<?>> comparator = new TieBreakerBindingComparator(context);
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

        public void setExplicit(boolean explicit) {
            isExplicit = explicit;
        }

        public boolean isImplicit() {
            return !isExplicit;
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

        BindingGraphFactory(SortContext context) {
            this.context = context;
        }

        public BindingGraph createGraph() {
            BindingGraph graph = new BindingGraph(context);

            context.bindings.forEach(graph::addVertex);
            createImplicitEdges(graph);
            createExplicitEdges(graph);
            repositionExplicitEdges(graph, RepositioningDirection.HEAD);
            repositionExplicitEdges(graph, RepositioningDirection.TAIL);

            return graph;
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

        private void repositionExplicitEdges(BindingGraph graph, RepositioningDirection direction) {
            for (Binding<?> binding : context.bindings) {
                List<BindingEdge> edges = new ArrayList<>(direction.getEdges(graph, binding));

                if (edges.size() > 1) {
                    // Here we make sure that elements explicitly ordered using
                    // @Order get BEFORE ones ordered implicitly (those by their multibinder order).
                    //
                    // Behavior:
                    // Consider a DAG with vertexes a, b, c, d.
                    //             +-------+
                    //             |       |
                    //             |   a   |
                    //             |       |
                    //     +-------+---+---+-------+
                    //     |           |           |
                    // +---v---+   +---v---+   +---v---+
                    // |       |   |       |   |       |
                    // |   b   |   |   c   |   |   d   |
                    // |  (i)  |   |  (e)  |   |  (e)  |
                    // +-------+   +-------+   +-------+
                    // Edges (a, b) are implicit and (a, c) and (a, d) are explicit.
                    // Only one implicit edge may be present.
                    //
                    // The goal is to:
                    //  * remove the edge between a and b
                    //  * create an edge from each explicit edge to b (c -> b, d -> b)
                    //
                    // The example above, once processed, should get this result:
                    //          +-------+
                    //          |       |
                    //          |   a   |
                    //          |       |
                    //     +----+-------+----+
                    //     |                 |
                    // +---v---+         +---v---+
                    // |       |         |       |
                    // |   c   |         |   d   |
                    // |  (e)  |         |  (e)  |
                    // +-------+----+----+-------+
                    //              |
                    //          +---v---+
                    //          |       |
                    //          |   b   |
                    //          |  (e)  |
                    //          +-------+
                    //
                    // We can also do it reverse by:
                    // * removing the edge between b and a
                    // * create an edge from b to each explicit edge (b -> c, b -> d)
                    List<BindingEdge> explicitEdges = new LinkedList<>();
                    BindingEdge implicitEdge = null;

                    for (BindingEdge edge : edges) {
                        if (edge.isImplicit()) {
                            if (implicitEdge != null) {
                                throw new IllegalStateException("There cannot be more than one implicit edge.");
                            }
                            implicitEdge = edge;
                        } else {
                            explicitEdges.add(edge);
                        }
                    }

                    if (implicitEdge != null && !explicitEdges.isEmpty()) {
                        Binding<?> newTarget = direction.getEndpoint(graph, implicitEdge);

                        graph.removeEdge(implicitEdge);

                        for (BindingEdge explicitEdge : explicitEdges) {
                            Binding<?> newSource = direction.getEndpoint(graph, explicitEdge);

                            direction.addEdge(graph, newSource, newTarget);
                        }
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

        private enum RepositioningDirection {
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

    private static final class TieBreakerBindingComparator implements Comparator<Binding<?>> {
        private final SortContext context;

        public TieBreakerBindingComparator(SortContext context) {
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
