package cca.procedure;

import cca.Node;
import cca.Position;
import cca.visitors.VisitorInterface;
import java.util.List;

public class TerminationOrder extends Node {

    private final List<OrderingCouple> elements;

    public TerminationOrder(List<OrderingCouple> elements, Position position) {
        this.elements = elements;
        super(position);
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }

}
