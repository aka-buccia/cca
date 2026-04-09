package cca.expression;

public sealed interface ConstantValue {
}

final record StringValue(String value) implements ConstantValue {
};

final record IntValue(Integer value) implements ConstantValue {
};
