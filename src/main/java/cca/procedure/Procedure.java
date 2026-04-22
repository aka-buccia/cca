package cca.procedure;

import java.util.List;

import cca.Node;
import cca.Position;
import cca.choreography.*;
import cca.visitors.VisitorInterface;

public class Procedure extends Node {

    private final String name;
    private final List<ProcedureParameter> parameters;
    private final TerminationOrder terminationOrder;
    private final Choreography choreography;

    public Procedure(
            String name,
            List<ProcedureParameter> parameters,
            TerminationOrder terminationOrder,
            Choreography choreography,
            Position position) {
        this.name = name;
        this.parameters = parameters;
        this.terminationOrder = terminationOrder;
        this.choreography = choreography;
        super(position);

    }

    public String id() {
        return this.name;
    }

    public List<ProcedureParameter> parameters() {
        return this.parameters;
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
