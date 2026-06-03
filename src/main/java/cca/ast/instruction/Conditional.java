package cca.ast.instruction;

import cca.ast.Position;
import cca.ast.Role;
import cca.ast.choreography.Choreography;
import cca.ast.expression.Expression;
import cca.ast.visitors.VisitorInterface;

public class Conditional extends Instruction {

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
