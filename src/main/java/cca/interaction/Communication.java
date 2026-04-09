package cca.interaction;

import cca.Role;
import cca.visitors.VisitorInterface;

public class Communication extends Interaction {

    private final Expression leftExpression;
    private final Expression rightExpression;
    private final Role leftRole;
    private final Role rightRole;

    public Communication(
            Expression leftExpression,
            Role leftRole,
            Expression rightExpression,
            Role rightRole,
            Position position) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
        this.leftRole = leftRole;
        this.rightRole = rightRole;
        super(position);
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }

}
