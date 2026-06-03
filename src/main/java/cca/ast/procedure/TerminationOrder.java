package cca.ast.procedure;

import cca.ast.Node;
import cca.ast.Position;
import cca.ast.visitors.VisitorInterface;

import java.util.Collections;
import java.util.List;

public class TerminationOrder extends Node {

    private final List<OrderingCouple> elements;

    public TerminationOrder(List<OrderingCouple> elements, Position position) {
        this.elements = elements;
        super(position);
    }

    public List<OrderingCouple> elements() {
        return this.elements;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }

    // Placeholder node for missing TerminationOrder
    public static class TerminationOrderDefault extends TerminationOrder {

        public TerminationOrderDefault(Position position) {
            super(Collections.emptyList(), position);
        }
    }

}
