package cca.interaction;

import cca.Position;
import cca.visitors.VisitorInterface;
import cca.procedure.ProcedureParameterList;

public class ProcedureCall extends Interaction {

    private final String name;
    private final ProcedureParameterList parameterList;

    public ProcedureCall(String name, ProcedureParameterList parameterList, Position position) {
        this.name = name;
        this.parameterList = parameterList;
        super(position);
    }

    public String name() {
        return this.name;
    }

    public ProcedureParameterList parameterList() {
        return this.parameterList;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
