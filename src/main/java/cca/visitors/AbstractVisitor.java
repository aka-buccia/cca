package cca.visitors;

import cca.Program;
import cca.procedure.*;

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
}
