package cca.ast.instruction;

import cca.ast.Position;
import cca.ast.Role;
import cca.ast.expression.Expression;
import cca.ast.visitors.VisitorInterface;

public class EndResponse extends End {

    private final Expression expression;
    private final Role targetRole;

    public EndResponse(Expression expression, Role endingRole, Role targetRole, Position position) {
        this.expression = expression;
        this.targetRole = targetRole;
        super(endingRole, position);
    }

    public Expression expression() {
        return this.expression;
    }

    public Role targetRole() {
        return this.targetRole;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
