package cca.procedure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import cca.Node;
import cca.Position;
import cca.visitors.VisitorInterface;

public class ProcedureParameterList extends Node {

    private final List<ProcedureParameter> parameters;
    private final HashSet<StatefulParameter> statefulParameters;
    private final HashSet<NonTerminatingParameter> nonTerminatingParameters;
    private final HashSet<TerminatingParameter> terminatingParameters;

    public ProcedureParameterList(HashSet<StatefulParameter> statefulParameters,
            HashSet<NonTerminatingParameter> nonTerminatingParameters,
            HashSet<TerminatingParameter> terminatingParameters, Position position) {

        this.statefulParameters = statefulParameters;
        this.nonTerminatingParameters = nonTerminatingParameters;
        this.terminatingParameters = terminatingParameters;
        super(position);
        fillParametersList();
    }

    private void fillParametersList() {
        this.parameters = new ArrayList<>();
        this.parameters.addAll(this.statefulParameters);
        this.parameters.addAll(this.nonTerminatingParameters);
        this.parameters.addAll(this.terminatingParameters);
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
