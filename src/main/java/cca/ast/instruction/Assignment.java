package cca.ast.instruction;

import cca.ast.Position;
import cca.ast.Role;
import cca.ast.expression.Expression;
import cca.ast.expression.Variable;
import cca.ast.visitors.VisitorInterface;

public class Assignment extends Instruction {

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
