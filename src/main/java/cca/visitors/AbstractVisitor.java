package cca.visitors;

import cca.Program;

public abstract class AbstractVisitor<T> implements VisitorInterface<T> {

    @Override
    public T visit(Program n) {
        throw new UnsupportedOperationException();
    }
}
