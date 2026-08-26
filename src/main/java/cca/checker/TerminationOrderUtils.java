package cca.checker;

import java.util.HashSet;
import java.util.Set;

import cca.ast.procedure.OrderingCouple;

public final class TerminationOrderUtils {

    private TerminationOrderUtils() {
    }

    public static Set<OrderingCouple> computeTransitiveClosure(Set<OrderingCouple> startingOrderSet) {
        Set<OrderingCouple> closure = new HashSet<>(startingOrderSet);

        boolean added;

        do {
            added = false;
            Set<OrderingCouple> toAdd = new HashSet<>();

            // For each couple (a, b) and (c, d)
            for (OrderingCouple c1 : closure) {
                for (OrderingCouple c2 : closure) {

                    // If b == c
                    if (c1.right().equals(c2.left())) {

                        // Create (a, d)
                        OrderingCouple newCouple = new OrderingCouple(
                                c1.left(),
                                c2.right(),
                                c1.position());

                        // If it hasn't been added yet, add it
                        if (!closure.contains(newCouple) && !toAdd.contains(newCouple)) {
                            toAdd.add(newCouple);
                            added = true;
                        }
                    }
                }
            }

            closure.addAll(toAdd);

        } while (added);

        return closure;
    }

    /**
     * Check if a TerminationOrder is a strict partial order.
     *
     * An order is strict partial if it is unreflective and transitive.
     *
     * @param terminationOrder
     * @return
     */
    public static boolean isStrictPartialOrder(Set<OrderingCouple> terminationOrder) {
        if (terminationOrder == null) {
            return true;
        }

        Set<OrderingCouple> baseSet = new HashSet<>(terminationOrder);

        Set<OrderingCouple> closure = computeTransitiveClosure(baseSet);

        // Verify unreflectiveness of the transitive closure:
        // If there exists a pair (a, b) in the closure where a.equals(b),
        // means that there is a loop (e.g. a < b and b < a), violating irreflectivity.
        for (OrderingCouple couple : closure) {
            if (couple.left().equals(couple.right())) {
                return false; // a<a found
            }
        }

        return true;
    }
}
