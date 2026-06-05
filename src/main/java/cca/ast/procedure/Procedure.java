package cca.ast.procedure;

import cca.ast.Node;
import cca.ast.Position;
import cca.ast.Name;
import cca.ast.choreography.Choreography;
import cca.ast.visitors.VisitorInterface;

public class Procedure extends Node {

    private final Name name;
    private final ProcedureParameterList parameterList;
    private final TerminationOrder terminationOrder;
    private final Choreography choreography;

    public Procedure(
            Name name,
            ProcedureParameterList parameterList,
            TerminationOrder terminationOrder,
            Choreography choreography,
            Position position) {
        this.name = name;
        this.parameterList = parameterList;
        this.terminationOrder = terminationOrder;
        this.choreography = choreography;
        super(position);

    }

    public Name name() {
        return this.name;
    }

    public ProcedureParameterList parameterList() {
        return this.parameterList;
    }

    public TerminationOrder terminationOrder() {
        return this.terminationOrder;
    }

    public Choreography choreography() {
        return this.choreography;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }

}
