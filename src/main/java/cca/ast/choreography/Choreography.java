package cca.ast.choreography;

import cca.ast.Node;
import cca.ast.Position;
import cca.ast.instruction.*;
import cca.ast.visitors.VisitorInterface;
import java.util.List;

public class Choreography extends Node {

    private final List<Instruction> instructions;
    private final Terminated termination;

    public Choreography(List<Instruction> instructions, Terminated termination, Position position) {
        this.instructions = instructions;
        this.termination = termination;
        super(position);
    }

    public List<Instruction> instructions() {
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
