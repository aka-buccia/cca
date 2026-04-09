package cca.visitors;

import cca.Program;
import cca.Role;
import cca.Variable;
import cca.procedure.*;
import cca.choreography.*;
import cca.interaction.*;

public abstract class AbstractVisitor<T> implements VisitorInterface<T> {

    @Override
    public T visit(Program n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(Procedure n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(TerminationOrder n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(OrderingCouple n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(Choreography n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(Terminated n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(Communication n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(Role n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(Variable n) {
        throw new UnsupportedOperationException();
    }

}
