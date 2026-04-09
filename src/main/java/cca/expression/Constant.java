package cca.expression;

import cca.Position;
import cca.visitors.VisitorInterface;

public class Constant<T extends ConstantValue> extends Expression {

    private final T value;

    public Constant(T value, Position position) {
        this.value = value;
        super(position);
    }

    // Factory methods
    public static Constant<StringValue> ofString(String s, Position position) {
        return new Constant<>(new StringValue(s), position);
    }

    public static Constant<IntValue> ofInt(int i, Position position) {
        return new Constant<>(new IntValue(i), position);
    }

    public T value() {
        return value;
    }

    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
