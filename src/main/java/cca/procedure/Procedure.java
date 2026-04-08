package cca.procedure;

import java.util.List;

import cca.Node;
import cca.Position;
import cca.visitors.VisitorInterface;

public class Procedure extends Node {

    private final String name;
    private final List<ProcedureParameter> parameters;
    private final TerminationOrder terminationOrder;
    private final Choreography choreography;
    private final Position position;

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

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }

}
