package cca.ast.procedure;

import cca.ast.Position;
import cca.ast.Role;
import cca.ast.visitors.VisitorInterface;

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
