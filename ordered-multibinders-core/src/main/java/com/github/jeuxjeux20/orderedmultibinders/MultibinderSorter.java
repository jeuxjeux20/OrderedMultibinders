package com.github.jeuxjeux20.orderedmultibinders;

import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBinding;
import com.github.jeuxjeux20.orderedmultibinders.internal.binding.OrderedBindingBiMapFactory;
import com.github.jeuxjeux20.orderedmultibinders.internal.binding.OrderedBindingFactory;
import com.github.jeuxjeux20.orderedmultibinders.util.MultibinderFinder;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binding;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.MultibinderBinding;
import com.google.inject.spi.Element;
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

    MultibinderSorter(List<Element> elements) {
        this.elements = elements;
    }

    public List<Binding<?>> sort(MultibinderBinding<?> multibinder) {
        List<Binding<?>> multiBinderElements = MultibinderFinder.findMultibinderContentBindings(elements, multibinder);

        if (multiBinderElements.isEmpty()) {
            return multiBinderElements;
        }

        BindingOrderGraphFactory graphFactory = new BindingOrderGraphFactory(multiBinderElements);
        BindingGraph graph = graphFactory.createGraph();

        return sortElements(multiBinderElements, graph);
    }

    private List<Binding<?>> sortElements(List<Binding<?>> multibinderElements, BindingGraph graph) {
        if (multibinderElements.size() < 2) {
            // There is no ordering to apply with under two elements.
            return multibinderElements;
        }

        // Create a binding positions map, pretty much like a cache for indexOf.
        Map<Binding<?>, Integer> bindingPositions = new HashMap<>();
        for (int i = 0; i < multibinderElements.size(); i++) {
            bindingPositions.put(multibinderElements.get(i), i);
        }

        // Then use the topological sorter to iterate the graph.
        TopologicalOrderIterator<Binding<?>, DefaultEdge> topologicalIterator =
                new TopologicalOrderIterator<>(graph, Comparator.comparing(bindingPositions::get));

        return ImmutableList.copyOf(topologicalIterator);
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
    private class BindingOrderGraphFactory {
        private final List<Binding<?>> bindings;
        private final BiMap<Binding<?>, OrderedBinding> orderedBindings;

        BindingOrderGraphFactory(List<Binding<?>> bindings) {
            this.bindings = bindings;
            this.orderedBindings = new OrderedBindingBiMapFactory(orderedBindingFactory).createOrderedBindings(bindings);
        }

        public BindingGraph createGraph() {
            BindingGraph graph = new BindingGraph();

            bindings.forEach(graph::addVertex);
            createEdges(graph);

            return graph;
        }

        private void createEdges(BindingGraph graph) {
            for (Binding<?> binding : ImmutableList.copyOf(graph.vertexSet())) {
                Order order = orderedBindings.get(binding).getOrder();
                if (order == null) continue;

                for (Class<?> beforeClass : order.before()) {
                    Binding<?> succeedingBinding = findByClassOrThrow(beforeClass);

                    try {
                        graph.addEdge(binding, succeedingBinding);
                    } catch (IllegalArgumentException e) {
                        throw cycleDetectedException(binding, succeedingBinding);
                    }
                }

                for (Class<?> afterClass : order.after()) {
                    Binding<?> precedingBinding = findByClassOrThrow(afterClass);

                    try {
                        graph.addEdge(precedingBinding, binding);
                    } catch (IllegalArgumentException e) {
                        throw cycleDetectedException(binding, precedingBinding);
                    }
                }
            }
        }

        private Binding<?> findByClassOrThrow(Class<?> clazz) {
            Binding<?> binding = orderedBindings.inverse().get(OrderedBinding.equalityToken(TypeLiteral.get(clazz)));

            if (binding != null) {
                return binding;
            } else {
                throw new UnableToResolveClassAsBindingException(clazz);
            }
        }

        private CycleDetectedException cycleDetectedException(Binding<?> binding, Binding<?> otherBinding) {
            OrderedBinding orderedBinding = orderedBindings.get(binding);
            OrderedBinding otherOrderedBinding = orderedBindings.get(otherBinding);

            return new CycleDetectedException(
                    "Cycle detected between " + orderedBinding.getIdentifier() +
                    " and " + otherOrderedBinding.getIdentifier() + ".");
        }
    }

    private static final class BindingGraph extends DirectedAcyclicGraph<Binding<?>, DefaultEdge> {
        BindingGraph() {
            super(DefaultEdge.class);
        }
    }
}
