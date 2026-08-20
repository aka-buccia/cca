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
    private CheckerContext context;
    private List<IllFormedException> errors;

    public LocalChecker() {
    }

    public LocalChecker(Map<String, ProcedureInfo> procedureMap, Procedure procedure, CheckerContext context,
            List<IllFormedException> errors) {
        this.procedureMap = procedureMap;
        this.procedure = procedure;
        this.context = context;
        this.errors = errors;
    }

    public List<IllFormedException> check(Map<String, ProcedureInfo> procedureMap, Procedure procedure) {

        this.procedureMap = procedureMap;
        this.procedure = procedure;
        this.context = new CheckerContext();

        this.context.init(procedure);
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

        List<TerminatingPair> terminatingPairs = context.getTerminatingPairs();

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
        if (!context.isStateful(n.leftRole())) {
            addError(n.leftRole());
        }

        // Right role has to be stateful
        if (!context.isStateful(n.rightRole())) {
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
        if (!context.isStateful(n.sourceRole())) {
            addError(n.sourceRole());
        }

        // Target role has to be stateful
        if (!context.isStateful(n.targetRole())) {
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
        if (n.targetRole() != null && !context.isStateful(n.targetRole())) {
            addError(n.targetRole());
        }

        addTerminatingPair(n.targetRole(), null);

        // Add target role to current scope
        context.addStateless(n.targetRole());

        return null;
    }

    @Override
    public Void visit(RequestResponse n) {

        // Source role has to be defined as stateful or nonterminating
        if (!isDefined(n.sourceRole())) {
            addError(n.sourceRole());
        }

        // Target role has to not be in the current scope
        if (n.targetRole() != null && !context.isStateful(n.targetRole())) {
            addError(n.targetRole());
        }

        addTerminatingPair(n.targetRole(), n.sourceRole());

        addOrderingCouple(n.targetRole(), n.sourceRole());
        computeTerminationOrderTransitiveClosure();

        // Add target role to current scope
        context.addStateless(n.targetRole());

        return null;
    }

    @Override
    public Void visit(End n) {

        // Ending role has to be a stateless role without creator
        if (isTerm(new TerminatingPair(n.endingRole(), null))) {
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
        if (isTerm(new TerminatingPair(n.endingRole(), null))) {
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

    @Override
    public Void visit(Conditional n) {

        // Guard role has to be defined
        if (!context.isDefined(n.targetRole())) {
            addError(n.targetRole());
        }

        return null;
    }

    // Helpers

    private void addTerminatingPair(Role created, Role creator) {
        context.addTerminatingPair(created, creator);
    }

    private void removeTerminatingPair(Role created, Role creator) {
        context.removeTerminatingPair(created, creator);
    }

    private boolean isTerm(TerminatingPair t) {
        return context.isTerm(t);
    }

    private void addOrderingCouple(Role left, Role right) {
        context.addOrderingCouple(left, right);
    }

    private void removeOrderingCouplesWithLeft(Role r) {
        context.removeOrderingCouplesWithLeft(r);
    }

    private Set<OrderingCouple> computeTerminationOrderTransitiveClosure() {
        return context.computeTransitiveClosure();
    }

    private boolean isDefined(Role r) {
        return context.isDefined(r);
    }

    private boolean isFree(Role r) {
        return context.isFree(r);
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
