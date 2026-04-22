package cca;

import java.util.List;
import cca.procedure.*;
import cca.visitors.VisitorInterface;

public class Program extends Node {

    private final List<Procedure> procedures;

    public Program(List<Procedure> procedures, Position position) {
        super(position);
        this.procedures = procedures;
    }

    public List<Procedure> procedures() {
        return procedures;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }

}
