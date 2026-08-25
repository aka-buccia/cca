package cca.checker;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import cca.ast.choreography.*;
import cca.ast.instruction.*;
import cca.ast.visitors.AbstractVisitor;

// Visitor for collecting terminated roles inside a choreography
public class TerminatingRolesCollector extends AbstractVisitor<Set<TerminatingPair>> {

    @Override
    public Set<TerminatingPair> visit(Choreography n) {
        Set<TerminatingPair> terminated = new HashSet<>();
        for (Instruction i : n.instructions()) {
            terminated.addAll(i.accept(this));
        }
        return terminated;
    }

    @Override
    public Set<TerminatingPair> visit(Instruction n) {
        return n.accept(this);
    }

    @Override
    public Set<TerminatingPair> visit(Request n) {
        return Collections.emptySet();
    }

    @Override
    public Set<TerminatingPair> visit(RequestResponse n) {
        return Collections.emptySet();
    }

    @Override
    public Set<TerminatingPair> visit(Selection n) {
        return Collections.emptySet();
    }

    @Override
    public Set<TerminatingPair> visit(Assignment n) {
        return Collections.emptySet();
    }

    @Override
    public Set<TerminatingPair> visit(ProcedureCall n) {
        // Terminating parameters of the procedure call
        Set<TerminatingPair> terminatingPairs = n.parameterList().terminatingParameters().stream()
                .map(tp -> new TerminatingPair(tp.createdRole(), tp.creatorRole(), tp.position()))
                .collect(Collectors.toSet());

        return terminatingPairs;
    }

    @Override
    public Set<TerminatingPair> visit(End n) {
        return Set.of(new TerminatingPair(n.endingRole(), null));
    }

    @Override
    public Set<TerminatingPair> visit(EndResponse n) {
        return Set.of(new TerminatingPair(n.endingRole(), n.targetRole()));
    }

    @Override
    public Set<TerminatingPair> visit(Conditional n) {

        // Calculate the roles ending in the two branches separately
        Set<TerminatingPair> thenTerminated = n.ifBranch().accept(this);
        Set<TerminatingPair> elseTerminated = n.elseBranch().accept(this);

        // Calculate the intersection: only those ending in both end branches
        Set<TerminatingPair> intersected = new HashSet<>(thenTerminated);
        intersected.retainAll(elseTerminated);

        return intersected;
    }

}
