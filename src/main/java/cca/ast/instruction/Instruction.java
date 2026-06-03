package cca.ast.instruction;

import cca.ast.Node;
import cca.ast.Position;

public abstract class Instruction extends Node {

    public Instruction(Position position) {
        super(position);
    }
}
