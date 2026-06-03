package cca.choreography;

import cca.Node;
import cca.Position;
import cca.instruction.*;
import cca.visitors.VisitorInterface;
import java.util.List;

public class Choreography extends Node {

    private final List<Instruction> instructions;
    private final Terminated termination;

    public Choreography(List<Instruction> instructions, Terminated termination, Position position) {
        this.instructions = instructions;
        this.termination = termination;
        super(position);
    }

    public List<Instruction> interactions() {
        return this.instructions;
    }

    public Terminated termination() {
        return this.termination;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
