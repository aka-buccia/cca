package cca.choreography;

import cca.Node;
import cca.Position;
import cca.interaction.*;
import cca.visitors.VisitorInterface;
import java.util.List;

public class Choreography extends Node {

    private final List<Interaction> interactions;
    private final Terminated termination;

    public Choreography(List<Interaction> interactions, Terminated termination, Position position) {
        this.interactions = interactions;
        this.termination = termination;
        super(position);
    }

    public List<Interaction> interactions() {
        return this.interactions;
    }

    public Terminated termination() {
        return this.termination;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
