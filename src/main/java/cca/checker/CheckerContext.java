package cca.checker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cca.ast.Role;
import cca.ast.procedure.*;

public class CheckerContext {

    private Set<Role> statefulRoles;
    private Set<Role> statelessRoles;
    private Set<Role> nonTerminatingRoles;
    private List<TerminatingPair> terminatingPairs;
    private Set<OrderingCouple> terminationOrder;

    public CheckerContext() {
        this.statefulRoles = new LinkedHashSet<>();
        this.statelessRoles = new LinkedHashSet<>();
        this.nonTerminatingRoles = new LinkedHashSet<>();
        this.terminatingPairs = new ArrayList<>();
        this.terminationOrder = new LinkedHashSet<>();
    }

    public void init(ProcedureSignature procedureSignature) {
        ProcedureParameterList p = procedureSignature.parameterList();

        this.terminatingPairs = p.terminatingParameters().stream()
                .map(tp -> new TerminatingPair(tp.createdRole(), tp.creatorRole(), tp.position()))
                .collect(Collectors.toCollection(ArrayList::new));

        this.statefulRoles = p.statefulParameters().stream()
                .map(StatefulParameter::parameter)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        this.nonTerminatingRoles = p.nonTerminatingParameters().stream()
                .map(NonTerminatingParameter::parameter)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        this.statelessRoles = Stream.concat(
                nonTerminatingRoles.stream(),
                p.terminatingParameters().stream()
                        .map(TerminatingParameter::createdRole))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        this.terminationOrder = new HashSet<>(procedureSignature.terminationOrder());
    }

    public CheckerContext copy() {
        CheckerContext copy = new CheckerContext();
        copy.statefulRoles = new LinkedHashSet<>(this.statefulRoles);
        copy.statelessRoles = new LinkedHashSet<>(this.statelessRoles);
        copy.nonTerminatingRoles = new LinkedHashSet<>(this.nonTerminatingRoles);
        copy.terminatingPairs = new ArrayList<>(this.terminatingPairs);
        copy.terminationOrder = new LinkedHashSet<>(this.terminationOrder);
        return copy;
    }

    // Getters
    public Set<Role> getStatefulRoles() {
        return statefulRoles;
    }

    public Set<Role> getStatelessRoles() {
        return statelessRoles;
    }

    public Set<Role> getNonTerminatingRoles() {
        return nonTerminatingRoles;
    }

    public List<TerminatingPair> getTerminatingPairs() {
        return terminatingPairs;
    }

    public Set<OrderingCouple> getTerminationOrder() {
        return terminationOrder;
    }

    public void setNonTerminatingRoles(Set<Role> nonTerminatingRoles) {
        this.nonTerminatingRoles = nonTerminatingRoles;
    }

    public void setTerminatingPairs(List<TerminatingPair> terminatingPairs) {
        this.terminatingPairs = terminatingPairs;
    }

    public void setTerminatingPairsFromParameters(List<TerminatingParameter> terminatingParameters) {
        this.terminatingPairs = terminatingParameters.stream()
                .map(tp -> new TerminatingPair(tp.createdRole(), tp.creatorRole(), tp.position()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void setStatelessRoles(Set<Role> statelessRoles) {
        this.statelessRoles = statelessRoles;
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

    public void computeTerminationOrderTransitiveClosure() {
        terminationOrder = TerminationOrderUtils.computeTransitiveClosure(terminationOrder);
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

    public boolean isNonTerm(Role r) {
        return nonTerminatingRoles.contains(r);
    }

    public void addStateless(Role r) {
        statelessRoles.add(r);
    }
}
