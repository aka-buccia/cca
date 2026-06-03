package cca.ast.choreography;

import cca.ast.Node;
import cca.ast.Position;
import cca.ast.visitors.VisitorInterface;

public class Terminated extends Node {

    public Terminated(Position position) {
        super(position);
    }

    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }

    // Placeholder node for implicit termination
    public static class TerminatedOmitted extends Terminated {

        public TerminatedOmitted(Position position) {
            super(position);
        }

    }
}
