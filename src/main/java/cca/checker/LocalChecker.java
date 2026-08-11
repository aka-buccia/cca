package cca.checker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cca.ast.Node;
import cca.ast.choreography.*;
import cca.ast.instruction.*;
import cca.ast.procedure.*;
import cca.ast.visitors.AbstractVisitor;
import cca.exceptions.IllFormedException;

public class LocalChecker extends AbstractVisitor<Void> {

    private Map<String, ProcedureInfo> procedureMap;
    private Procedure procedure;

    private List<StatefulParameter> statefulParameters;
    private List<NonTerminatingParameter> nonTerminatingParameters;
    private List<TerminatingParameter> terminatingParameters;
    private TerminationOrder terminationOrder;
    private List<ProcedureParameter> statelessParameters;

    private List<IllFormedException> errors;

    // public LocalChecker(Map<String, ProcedureInfo> procedureTable, Procedure
    // procedure) {
    // this.procedureTable = procedureTable;
    // this.procedure = procedure;
    //
    // var p = procedure.parameterList();
    // this.statefulParameters = p.statefulParameters();
    // this.nonTerminatingParameters = p.nonTerminatingParameters();
    // this.terminatingParameters = p.terminatingParameters();
    // this.terminationOrder = procedure.terminationOrder();
    //
    // this.statelessParameters = new ArrayList<>(nonTerminatingParameters);
    // this.statelessParameters.addAll(terminatingParameters);
    //
    // this.errors = new ArrayList<>();
    // }

    private void extractContext() {

        ProcedureParameterList p = procedure.parameterList();
        this.statefulParameters = p.statefulParameters();
        this.nonTerminatingParameters = p.nonTerminatingParameters();
        this.terminatingParameters = p.terminatingParameters();
        this.terminationOrder = procedure.terminationOrder();

        this.statelessParameters = new ArrayList<>(nonTerminatingParameters);
        this.statelessParameters.addAll(terminatingParameters);
    }

    public List<IllFormedException> check(Map<String, ProcedureInfo> procedureMap, Procedure procedure) {

        this.procedureMap = procedureMap;
        this.procedure = procedure;
        this.errors = new ArrayList<>();

        extractContext();
        visit(procedure.choreography());

        return errors;
    }

    @Override
    public Void visit(Choreography n) {

        for (Instruction i : n.instructions()) {
            visit(i);
        }
        visit(n.termination());

        return null;
    }

    @Override
    public Void visit(Terminated n) {

        // All terminating roles has to terminate before procedure termination
        if (!terminatingParameters.isEmpty()) {
            addError(n);
        }

        return null;
    }

    // Helpers

    private void addError(Node n) {
        addError(n, "");
    }

    private void addError(Node n, String message) {
        errors.add(
                new IllFormedException(
                        n.position(),
                        message));
    }

}
