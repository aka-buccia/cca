package cca.procedure;

import java.util.List;

import cca.Node;
import cca.Position;
import cca.visitors.VisitorInterface;

public class ProcedureCall extends Node {

    private final String name;
    private final List<ProcedureParameter> parameters;

    public ProcedureCall(String name, List<ProcedureParameter> parameters, Position position) {
        this.name = name;
        this.parameters = parameters;
        super(position);
    }

    public String name() {
        return this.name;
    }

    public List<ProcedureParameter> parameters() {
        return this.parameters;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
