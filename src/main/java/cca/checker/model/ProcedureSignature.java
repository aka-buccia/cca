package cca.checker.model;

import cca.ast.procedure.ProcedureParameterList;

public record ProcedureSignature(
        ProcedureParameterList parameterList,
        ComputedTerminationOrder terminationOrder) {
}
