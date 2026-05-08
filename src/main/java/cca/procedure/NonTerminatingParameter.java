package cca.procedure;

import cca.Position;
import cca.Role;
import cca.visitors.VisitorInterface;

public class NonTerminatingParameter extends ProcedureParameter {

    private final Role parameter;

    public NonTerminatingParameter(Role parameter, Position position) {
        this.parameter = parameter;
        super(position);
    }

    public Role parameter() {
        return this.parameter;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
