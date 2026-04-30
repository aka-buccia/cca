package cca.interaction;

import cca.Position;
import cca.Role;
import cca.visitors.VisitorInterface;

public class End extends Interaction {

    private final Role endingRole;

    public End(Role endingRole, Position position) {
        this.endingRole = endingRole;
        super(position);
    }

    public Role endingRole() {
        return this.endingRole;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
