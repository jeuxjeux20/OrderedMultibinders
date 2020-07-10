package com.github.jeuxjeux20.orderedmultibinders.config;

import com.github.jeuxjeux20.orderedmultibinders.Order;
import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBinding;

/**
 * Gives a default position value for an {@link OrderedBinding} when it has no @{@link Order}
 * annotation or when its {@link Order#position()} value is 0.
 */
public interface DefaultPositionProvider {
    /**
     * Returns 0.
     */
    DefaultPositionProvider ZERO = b -> 0;
    /**
     * Tries to put the binding closer to its order constraints with the following behavior:
     * <ul>
     *     <li>When there is no @{@link Order} annotation, returns 0.</li>
     *     <li>When {@link Order#after()} and {@link Order#before()} are both not empty or both empty
     *     returns 0.</li>
     *     <li>When only {@link Order#before()} is specified, returns 1.</li>
     *     <li>When only {@link Order#after()} is specified, returns -1.</li>
     * </ul>
     */
    DefaultPositionProvider CLOSEST = binding -> {
        Order order = binding.getOrder();
        if (order == null) {
            return 0;
        }

        if ((order.before().length != 0 && order.after().length != 0) ||
            (order.before().length == 0 && order.after().length == 0)) {
            return 0;
        } else if (order.before().length != 0) {
            return 1;
        } else {
            return -1;
        }
    };

    int get(OrderedBinding binding);
}
