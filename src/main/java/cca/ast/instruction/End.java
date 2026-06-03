package cca.ast.instruction;

import cca.ast.Position;
import cca.ast.Role;
import cca.ast.visitors.VisitorInterface;

public class End extends Instruction {

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
