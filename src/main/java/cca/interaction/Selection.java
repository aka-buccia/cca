package cca.interaction;

import cca.Position;
import cca.Role;
import cca.Label;
import cca.visitors.VisitorInterface;

public class Selection extends Interaction {

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
