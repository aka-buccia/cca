package cca.interaction;

import cca.Position;
import cca.Role;
import cca.choreography.Choreography;
import cca.expression.Expression;
import cca.visitors.VisitorInterface;

public class Conditional extends Interaction {

    private final Expression condition;
    private final Role targetRole;
    private final Choreography ifBranch;
    private final Choreography elseBranch;

    public Conditional(Expression condition, Role targetRole, Choreography ifBranch, Choreography elseBranch,
            Position position) {
        this.condition = condition;
        this.targetRole = targetRole;
        this.ifBranch = ifBranch;
        this.elseBranch = elseBranch;
        super(position);
    }

    public Expression condition() {
        return this.condition;
    }

    public Role targetRole() {
        return this.targetRole;
    }

    public Choreography ifBranch() {
        return this.ifBranch;
    }

    public Choreography elseBranch() {
        return this.elseBranch;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }

}
