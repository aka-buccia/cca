package cca.ast.procedure;

import cca.ast.Position;
import cca.ast.Role;
import cca.ast.visitors.VisitorInterface;

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
