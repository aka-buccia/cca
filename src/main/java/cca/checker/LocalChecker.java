package cca.checker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cca.ast.Node;
import cca.ast.Role;
import cca.ast.choreography.*;
import cca.ast.instruction.*;
import cca.ast.procedure.*;
import cca.ast.visitors.AbstractVisitor;
import cca.exceptions.IllFormedException;

public class LocalChecker extends AbstractVisitor<Void> {

    private Map<String, ProcedureInfo> procedureMap;
    private Procedure procedure;

    private Set<Role> statefulRoles;
    private Set<Role> statelessRoles;

    private List<TerminatingPair> terminatingPairs;
    private Set<OrderingCouple> terminationOrder;

    private List<IllFormedException> errors;

    // Set of roles
    private void extractContext() {

        ProcedureParameterList p = procedure.parameterList();

        this.statefulRoles = p.statefulParameters().stream()
                .map(StatefulParameter::parameter)
                .collect(Collectors.toSet());

        this.statelessRoles = Stream.concat(
                p.nonTerminatingParameters().stream()
                        .map(NonTerminatingParameter::parameter),
                p.terminatingParameters().stream()
                        .map(TerminatingParameter::createdRole))
                .collect(Collectors.toSet());

        this.terminatingPairs = p.terminatingParameters().stream()
                .map(tp -> new TerminatingPair(tp.createdRole(), tp.creatorRole(), tp.position()))
                .collect(Collectors.toCollection(ArrayList::new));
        this.terminationOrder = new HashSet<>(procedure.terminationOrder().elements());
    }

    public List<IllFormedException> check(Map<String, ProcedureInfo> procedureMap, Procedure procedure) {

        this.procedureMap = procedureMap;
        this.procedure = procedure;
        this.errors = new ArrayList<>();

        extractContext();
        visit(procedure.choreography());

        return errors;
    }

    @Override
    public Void visit(Choreography n) {

        for (Instruction i : n.instructions()) {
            visit(i);
        }
        visit(n.termination());

        return null;
    }

    @Override
    public Void visit(Terminated n) {

        // All terminating roles has to terminate before procedure termination
        // TODO: iterate on every role which hasn't been terminated
        if (!terminatingPairs.isEmpty()) {
            addError(n);
        }

        return null;
    }

    @Override
    public Void visit(Instruction n) {

        n.accept(this);
        return null;
    }

    @Override
    public Void visit(Communication n) {

        // Stateful roles has to be different
        if (n.leftRole().equals(n.rightRole())) {
            addError(n);
        }

        // Left role has to be stateful
        if (!statefulRoles.contains(n.leftRole())) {
            addError(n.leftRole());
        }

        // Right role has to be stateful
        if (!statefulRoles.contains(n.rightRole())) {
            addError(n.rightRole());
        }

        return null;
    }

    @Override
    public Void visit(Selection n) {

        // Stateful roles has to be different
        if (n.sourceRole().equals(n.targetRole())) {
            addError(n);
        }

        // Source role has to be stateful
        if (!statefulRoles.contains(n.sourceRole())) {
            addError(n.sourceRole());
        }

        // Target role has to be stateful
        if (!statefulRoles.contains(n.targetRole())) {
            addError(n.targetRole());
        }

        return null;
    }

    @Override
    public Void visit(Assignment n) {

        // Target role has to be defined as stateful or nonterminating
        if (!isDefined(n.targetRole())) {
            addError(n.targetRole());
        }

        return null;
    }

    @Override
    public Void visit(Request n) {

        // Source role has to be defined as stateful or nonterminating
        if (!isDefined(n.sourceRole())) {
            addError(n.sourceRole());
        }

        // Target role has to not be in the current scope
        if (n.targetRole() != null && !statelessRoles.contains(n.targetRole())) {
            addError(n.targetRole());
        }

        addTerminatingPair(n.targetRole(), null);

        // Add target role to current scope
        statelessRoles.add(n.targetRole());

        return null;
    }

    @Override
    public Void visit(RequestResponse n) {

        // Source role has to be defined as stateful or nonterminating
        if (!isDefined(n.sourceRole())) {
            addError(n.sourceRole());
        }

        // Target role has to not be in the current scope
        if (n.targetRole() != null && !statelessRoles.contains(n.targetRole())) {
            addError(n.targetRole());
        }

        addTerminatingPair(n.targetRole(), n.sourceRole());

        addOrderingCouple(n.targetRole(), n.sourceRole());
        this.terminationOrder = computeTransitiveClosure(terminationOrder);

        // Add target role to current scope
        statelessRoles.add(n.targetRole());

        return null;
    }

    @Override
    public Void visit(End n) {

        // Ending role has to be a stateless role without creator
        if (terminatingPairs.contains(createTerm(n.endingRole(), null))) {
            addError(n.endingRole());
        }

        // Ending role has to be free from waiting a response
        if (!isFree(n.endingRole())) {
            addError(n.endingRole());
        }

        removeTerminatingPair(n.endingRole(), null);

        return null;
    }

    @Override
    public Void visit(EndResponse n) {

        // Ending role has to be a stateless role without creator
        if (terminatingPairs.contains(createTerm(n.endingRole(), null))) {
            addError(n.endingRole());
        }

        // Ending role has to be free from waiting a response
        if (!isFree(n.endingRole())) {
            addError(n.endingRole());
        }

        removeTerminatingPair(n.endingRole(), n.targetRole());
        removeOrderingCouplesWithLeft(n.endingRole());

        return null;
    }

    // Helpers

    private TerminatingPair createTerm(Role created, Role creator) {
        return new TerminatingPair(
                created,
                creator,
                created.position());
    }

    private void addTerminatingPair(Role created, Role creator) {
        TerminatingPair t = createTerm(created, creator);

        terminatingPairs.add(t);
    }

    private void removeTerminatingPair(Role created, Role creator) {
        TerminatingPair t = createTerm(created, creator);

        terminatingPairs.remove(t);
    }

    private void addOrderingCouple(Role left, Role right) {
        OrderingCouple o = new OrderingCouple(left, right, left.position());

        terminationOrder.add(o);
    }

    private void removeOrderingCouplesWithLeft(Role r) {
        terminationOrder.removeIf(couple -> couple.left().equals(r));
    }

    private Set<OrderingCouple> computeTransitiveClosure(Set<OrderingCouple> partialOrder) {
        Set<OrderingCouple> closure = new HashSet<>(partialOrder);

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

    private boolean isDefined(Role r) {
        return statefulRoles.contains(r) || statelessRoles.contains(r);
    }

    private boolean isFree(Role r) {
        return terminationOrder.stream()
                .map(OrderingCouple::right)
                .noneMatch(role -> role.equals(r));
    }

    private void addError(Node n) {
        addError(n, "");
    }

    private void addError(Node n, String message) {
        errors.add(
                new IllFormedException(
                        n.position(),
                        message));
    }

}
