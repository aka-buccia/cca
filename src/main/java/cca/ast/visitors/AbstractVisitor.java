package cca.ast.visitors;

import cca.ast.Program;
import cca.ast.Role;
import cca.ast.Media;
import cca.ast.Label;
import cca.ast.choreography.Choreography;
import cca.ast.choreography.Terminated;
import cca.ast.expression.Constant;
import cca.ast.expression.LocalFunction;
import cca.ast.expression.Variable;
import cca.ast.procedure.*;
import cca.ast.instruction.*;

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
    public T visit(Request n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(Selection n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(Assignment n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(StatefulParameter n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(NonTerminatingParameter n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(TerminatingParameter n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(End n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(EndResponse n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(Conditional n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(ProcedureCall n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(ProcedureParameterList n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(Constant<?> n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(LocalFunction n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(Role n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(Media n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(Variable n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T visit(Label n) {
        throw new UnsupportedOperationException();
    }

}
