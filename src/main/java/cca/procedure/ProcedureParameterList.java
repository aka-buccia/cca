package cca.procedure;

import java.util.ArrayList;
import java.util.List;

import cca.Node;
import cca.Position;
import cca.visitors.VisitorInterface;

public class ProcedureParameterList extends Node {

    private final List<ProcedureParameter> parameters;
    private final List<StatefulParameter> statefulParameters;
    private final List<NonTerminatingParameter> nonTerminatingParameters;
    private final List<TerminatingParameter> terminatingParameters;

    public ProcedureParameterList(List<StatefulParameter> statefulParameters,
            List<NonTerminatingParameter> nonTerminatingParameters,
            List<TerminatingParameter> terminatingParameters, Position position) {

        this.statefulParameters = statefulParameters;
        this.nonTerminatingParameters = nonTerminatingParameters;
        this.terminatingParameters = terminatingParameters;
        super(position);
        this.parameters = createParametersList();
    }

    private List<ProcedureParameter> createParametersList() {
        List<ProcedureParameter> newParameterList = new ArrayList<>();
        this.parameters.addAll(this.statefulParameters);
        this.parameters.addAll(this.nonTerminatingParameters);
        this.parameters.addAll(this.terminatingParameters);

        return newParameterList;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
