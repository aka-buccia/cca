package cca.procedure;

import cca.Position;
import cca.Role;
import cca.visitors.VisitorInterface;

public class StatefulParameter extends ProcedureParameter {

    private final Role parameter;

    public StatefulParameter(Role parameter, Position position) {
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
