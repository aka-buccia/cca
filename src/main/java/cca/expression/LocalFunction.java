package cca.expression;

import java.util.List;

import cca.visitors.VisitorInterface;
import cca.Position;

public class LocalFunction extends Expression {

    private final String id;
    private final List<Expression> parameters;

    public LocalFunction(String id, List<Expression> parameters, Position position) {
        this.id = id;
        this.parameters = parameters;
        super(position);
    }

    public String id() {
        return this.id;
    }

    public List<Expression> parameters() {
        return this.parameters;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
