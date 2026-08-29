package cca.checker;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import cca.ast.procedure.OrderingCouple;
import cca.exceptions.IllFormedException;
import cca.ast.Role;

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

    /**
     * Verify unreflexivity on a set already transitively closed.
     * The absence of self-dependencies (a < a) on a transitively closed set
     * ensures that the order is a strict partial order.
     */
    public static boolean isStrictPartialOrderOnClosedSet(ComputedTerminationOrder closedTerminationOrder) {
        if (closedTerminationOrder == null) {
            return true;
        }

        Set<OrderingCouple> terminationOrder = new HashSet<>(closedTerminationOrder.getOrderingCouples());

        for (OrderingCouple couple : terminationOrder) {
            if (couple.left().equals(couple.right())) {
                return false; // a < a found
            }
        }

        return true;
    }

    public static List<IllFormedException> validateTerminationOrderInvariants(
            Set<OrderingCouple> terminationOrder,
            List<Role> statefulRoles,
            List<Role> nonTerminatingRoles,
            List<TerminatingPair> terminatingPairs) {

        List<IllFormedException> errors = new ArrayList<>();
        Set<Role> statefulSet = new HashSet<>(statefulRoles);
        Set<Role> nonTermSet = new HashSet<>(nonTerminatingRoles);

        Set<Role> createdTerminatingRoles = terminatingPairs.stream()
                .map(TerminatingPair::createdRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Role> validCreatorRoles = new HashSet<>(statefulSet);
        validCreatorRoles.addAll(nonTermSet);
        validCreatorRoles.addAll(createdTerminatingRoles);

        // All second elements of terminating pairs must be stateful,
        // nonterm, term, or null
        for (TerminatingPair tp : terminatingPairs) {
            Role creator = tp.creatorRole();
            if (creator != null && !validCreatorRoles.contains(creator)) {
                errors.add(new IllFormedException(tp.position(),
                        "Creator role " + creator + " must be stateful, non-terminating, or terminating"));
            }
        }

        // Termination order must include all terminating pairs, excluding those with
        // 0 as the second element
        for (TerminatingPair tp : terminatingPairs) {
            if (tp.creatorRole() != null) {
                boolean containsPair = terminationOrder.stream()
                        .anyMatch(c -> c.left().equals(tp.createdRole()) && c.right().equals(tp.creatorRole()));

                if (!containsPair) {
                    errors.add(new IllFormedException(tp.position(),
                            "Termination order missing pair for terminating parameter: ("
                                    + tp.createdRole() + ", " + tp.creatorRole() + ")"));
                }
            }
        }

        for (OrderingCouple couple : terminationOrder) {
            // For every ordering couple it must exist a terminating pair with the same left
            // role
            if (!createdTerminatingRoles.contains(couple.left())) {
                errors.add(new IllFormedException(couple.position(), "Left role of ordering couple " + couple.left()
                        + " must be a terminating role"));
            }

            // The left role of an ordering couple must be a stateful, non term or left term
            // role
            if (!validCreatorRoles.contains(couple.right())) {
                errors.add(
                        new IllFormedException(couple.position(), "Right element of ordering couple " + couple.right()
                                + " is not a valid role (must be stateful, non-terminating, or terminating)"));
            }
        }

        // For every terminating pair (f, n) with n != 0, it cannot exist the ordering
        // couple (n, f) in the termination order
        for (TerminatingPair tp : terminatingPairs) {
            Role f = tp.createdRole();
            Role n = tp.creatorRole();

            if (n != null) {
                boolean hasInverseCycle = terminationOrder.stream()
                        .anyMatch(c -> c.left().equals(n) && c.right().equals(f));

                if (hasInverseCycle) {
                    errors.add(new IllFormedException(tp.position(), "Termination order cannot contain inverse couple ["
                            + n + ", " + f + "] for terminating pair [" + f + ", " + n + "]"));
                }
            }
        }

        return errors;
    }

}
