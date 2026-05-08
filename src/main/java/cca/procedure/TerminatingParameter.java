package cca.procedure;

import cca.Position;
import cca.Role;
import cca.visitors.VisitorInterface;

public class TerminatingParameter extends ProcedureParameter {

    private final Role createdRole;
    private final Role creatorRole;

    public TerminatingParameter(Role createdRole, Role creatorRole, Position position) {
        this.createdRole = createdRole;
        this.creatorRole = creatorRole;
        super(position);
    }

    public Role createdRole() {
        return this.createdRole;
    }

    public Role creatorRole() {
        return this.creatorRole;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
