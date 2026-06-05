package cca.ast.expression;

import java.util.List;

import cca.ast.visitors.VisitorInterface;
import cca.ast.Position;
import cca.ast.Name;

public class LocalFunction extends Expression {

    private final Name name;
    private final List<Expression> parameters;

    public LocalFunction(Name name, List<Expression> parameters, Position position) {
        this.name = name;
        this.parameters = parameters;
        super(position);
    }

    public Name name() {
        return this.name;
    }

    public List<Expression> parameters() {
        return this.parameters;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
