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

    public Role left() {
        return this.left;
    }

    public Role right() {
        return this.right;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof OrderingCouple) {
            return this.left.equals(((OrderingCouple) o).left())
                    & this.right.equals(((OrderingCouple) o).right());
        } else {
            return false;
        }
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
