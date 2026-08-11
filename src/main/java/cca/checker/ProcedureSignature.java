package cca.checker;

import cca.ast.procedure.ProcedureParameterList;
import cca.ast.procedure.TerminationOrder;

public record ProcedureSignature(
        ProcedureParameterList parameterList,
        TerminationOrder terminationOrder) {
}
