package cca.interaction;

import cca.Role;
import cca.Position;
import cca.expression.*;
import cca.visitors.VisitorInterface;

public class Communication extends Interaction {

    private final Expression expression;
    private final Variable variable;
    private final Role leftRole;
    private final Role rightRole;

    public Communication(
            Expression expression,
            Role leftRole,
            Variable variable,
            Role rightRole,
            Position position) {
        this.expression = expression;
        this.variable = variable;
        this.leftRole = leftRole;
        this.rightRole = rightRole;
        super(position);
    }

    public Role leftRole() {
        return this.leftRole;
    }

    public Role rightRole() {
        return this.rightRole;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }

}
