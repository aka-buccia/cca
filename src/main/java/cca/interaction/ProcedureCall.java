package cca.interaction;

import cca.Position;
import cca.visitors.VisitorInterface;
import cca.procedure.ProcedureParameterList;

public class ProcedureCall extends Interaction {

    private final String name;
    private final ProcedureParameterList parameters;

    public ProcedureCall(String name, ProcedureParameterList parameters, Position position) {
        this.name = name;
        this.parameters = parameters;
        super(position);
    }

    public String name() {
        return this.name;
    }

    public ProcedureParameterList parameters() {
        return this.parameters;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
