package cca.interaction;

import cca.Position;
import cca.Role;
import cca.expression.Expression;
import cca.expression.Variable;
import cca.visitors.VisitorInterface;

public class Assignment extends Interaction {

    private final Variable variable;
    private final Role targetRole;
    private final Expression expression;

    public Assignment(Variable variable, Role targetRole, Expression expression, Position position) {
        this.variable = variable;
        this.targetRole = targetRole;
        this.expression = expression;
        super(position);
    }

    public Variable variable() {
        return this.variable;
    }

    public Role targetRole() {
        return this.targetRole;
    }

    public Expression expression() {
        return this.expression;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
