package cca.ast.expression;

import cca.ast.Position;
import cca.ast.visitors.VisitorInterface;

public abstract class Constant<T> extends Expression {

    private final T value;

    public Constant(T value, Position position) {
        this.value = value;
        super(position);
    }

    public T value() {
        return this.value;
    }

    public static class ConstantInt extends Constant<Integer> {

        public ConstantInt(Integer value, Position position) {

            super(value, position);
        }

        @Override
        public <R> R accept(VisitorInterface<R> v) {
            return v.visit(this);
        }

    }

    public static class ConstantString extends Constant<String> {

        public ConstantString(String value, Position position) {

            super(value, position);
        }

        @Override
        public <R> R accept(VisitorInterface<R> v) {
            return v.visit(this);
        }

    }

}
