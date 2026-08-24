package cca.checker;

import cca.ast.procedure.ProcedureParameterList;
import cca.ast.procedure.OrderingCouple;

import java.util.Set;

public record ProcedureSignature(
        ProcedureParameterList parameterList,
        Set<OrderingCouple> terminationOrder) {
}
