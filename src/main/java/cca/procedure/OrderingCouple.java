package cca.procedure;

import cca.Node;
import cca.Position;
import cca.Role;
import cca.visitors.VisitorInterface;

public class OrderingCouple extends Node {

    private final Role left;
    private final Role right;

    public OrderingCouple(Role left, Role right, Position position) {
        this.left = left;
        this.right = right;
        super(position);
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
