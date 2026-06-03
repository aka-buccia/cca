package cca.ast.instruction;

import cca.ast.Position;
import cca.ast.visitors.VisitorInterface;
import cca.ast.procedure.ProcedureParameterList;

public class ProcedureCall extends Instruction {

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
