package cca.procedure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
        Stream.of(this.statefulParameters, this.nonTerminatingParameters, this.terminatingParameters)
                .filter(list -> list != null && !list.isEmpty()).forEach(newParameterList::addAll);

        return newParameterList;
    }

    public List<StatefulParameter> statefulParameters() {
        return this.statefulParameters;
    }

    public List<NonTerminatingParameter> statelessParameters() {
        return this.nonTerminatingParameters;
    }

    public List<TerminatingParameter> terminatingParameters() {
        return this.terminatingParameters;
    }

    public int size() {
        return parameters.size();
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }
}
