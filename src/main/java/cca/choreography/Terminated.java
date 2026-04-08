package cca.choreography;

import cca.Node;
import cca.Position;
import cca.visitors.VisitorInterface;

public class Terminated extends Node {

    public Terminated(Position position) {
        super(position);
    }

    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
