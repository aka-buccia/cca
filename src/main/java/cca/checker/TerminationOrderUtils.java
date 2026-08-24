package cca.checker;

import java.util.HashSet;
import java.util.Set;

import cca.ast.procedure.OrderingCouple;
import cca.ast.procedure.ProcedureParameterList;
import cca.ast.procedure.TerminationOrder;

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

    public static Set<OrderingCouple> buildCompleteTerminationOrder(
            ProcedureParameterList params,
            TerminationOrder declaredOrder) {

        Set<OrderingCouple> baseCouples = new HashSet<>(declaredOrder.elements());

        // add terminatingPairs as OrderingCouples
        params.terminatingParameters().stream()
                .filter(tp -> tp.creatorRole() != null)
                .map(tp -> new OrderingCouple(tp.createdRole(), tp.creatorRole(), tp.position()))
                .forEach(baseCouples::add);

        return computeTransitiveClosure(baseCouples);
    }
}
