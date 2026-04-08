package cca;

import cca.visitors.VisitorInterface;

public abstract class Node {

    private Position position;

    protected Node(Position position) {
        this.position = position;
    }

    public Position position() {
        return this.position;
    }

    public boolean hasPosition() {
        return position() != null;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public <R extends Node> R copyPosition(Node n) {
        this.position = n.position;
        return (R) this;
    }

    public abstract <R> R accept(VisitorInterface<R> v);
}
