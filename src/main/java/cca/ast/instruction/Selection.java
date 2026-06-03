package cca.ast.instruction;

import cca.ast.Position;
import cca.ast.Role;
import cca.ast.Label;
import cca.ast.visitors.VisitorInterface;

public class Selection extends Instruction {

    private final Role sourceRole;
    private final Role targetRole;
    private final Label label;

    public Selection(Role sourceRole, Role targetRole, Label label, Position position) {

        this.sourceRole = sourceRole;
        this.targetRole = targetRole;
        this.label = label;
        super(position);
    }

    public Role sourceRole() {
        return this.sourceRole;
    }

    public Role targetRole() {
        return this.targetRole;
    }

    public Label label() {
        return this.label;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }

}
