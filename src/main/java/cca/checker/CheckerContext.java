package cca.checker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cca.ast.Role;
import cca.ast.procedure.*;

public class CheckerContext {

    private Set<Role> statefulRoles;
    private Set<Role> statelessRoles;
    private List<TerminatingPair> terminatingPairs;
    private Set<OrderingCouple> terminationOrder;

    public CheckerContext() {
        this.statefulRoles = new HashSet<>();
        this.statelessRoles = new HashSet<>();
        this.terminatingPairs = new ArrayList<>();
        this.terminationOrder = new HashSet<>();
    }

    public void init(Procedure procedure) {
        ProcedureParameterList p = procedure.parameterList();

        this.terminatingPairs = p.terminatingParameters().stream()
                .map(tp -> new TerminatingPair(tp.createdRole(), tp.creatorRole(), tp.position()))
                .collect(Collectors.toCollection(ArrayList::new));

        this.statefulRoles = p.statefulParameters().stream()
                .map(StatefulParameter::parameter)
                .collect(Collectors.toSet());

        this.statelessRoles = Stream.concat(
                p.nonTerminatingParameters().stream()
                        .map(NonTerminatingParameter::parameter),
                p.terminatingParameters().stream()
                        .map(TerminatingParameter::createdRole))
                .collect(Collectors.toSet());

        this.terminationOrder = new HashSet<>(procedure.terminationOrder().elements());
    }

    public CheckerContext copy() {
        CheckerContext copy = new CheckerContext();
        copy.statefulRoles = new HashSet<>(this.statefulRoles);
        copy.statelessRoles = new HashSet<>(this.statelessRoles);
        copy.terminatingPairs = new ArrayList<>(this.terminatingPairs);
        copy.terminationOrder = new HashSet<>(this.terminationOrder);
        return copy;
    }

    // Getters
    public Set<Role> getStatefulRoles() {
        return statefulRoles;
    }

    public Set<Role> getStatelessRoles() {
        return statelessRoles;
    }

    public List<TerminatingPair> getTerminatingPairs() {
        return terminatingPairs;
    }

    public Set<OrderingCouple> getTerminationOrder() {
        return terminationOrder;
    }

    public void setTerminatingPairs(List<TerminatingPair> terminatingPairs) {
        this.terminatingPairs = terminatingPairs;
    }

    public void setTerminatingPairsFromParameters(List<TerminatingParameter> terminatingParameters) {
        this.terminatingPairs = terminatingParameters.stream()
                .map(tp -> new TerminatingPair(tp.createdRole(), tp.creatorRole(), tp.position()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Helpers

    public void addTerminatingPair(Role created, Role creator) {
        TerminatingPair t = new TerminatingPair(created, creator);

        terminatingPairs.add(t);
    }

    public void removeTerminatingPair(Role created, Role creator) {
        TerminatingPair t = new TerminatingPair(created, creator);

        terminatingPairs.remove(t);
    }

    public void addOrderingCouple(Role left, Role right) {
        OrderingCouple o = new OrderingCouple(left, right, left.position());

        terminationOrder.add(o);
    }

    public void removeOrderingCouplesWithLeft(Role r) {
        terminationOrder.removeIf(couple -> couple.left().equals(r));
    }

    public Set<OrderingCouple> computeTransitiveClosure() {
        Set<OrderingCouple> closure = new HashSet<>(terminationOrder);

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

    public boolean isDefined(Role r) {
        return statefulRoles.contains(r) || statelessRoles.contains(r);
    }

    public boolean isFree(Role r) {
        return terminationOrder.stream()
                .map(OrderingCouple::right)
                .noneMatch(role -> role.equals(r));
    }

    public boolean isStateful(Role r) {
        return statefulRoles.contains(r);
    }

    public boolean isTerm(TerminatingPair t) {
        return terminatingPairs.contains(t);
    }

    public void addStateless(Role r) {
        statelessRoles.add(r);
    }
}
