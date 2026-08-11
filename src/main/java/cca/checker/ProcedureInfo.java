package cca.checker;

import cca.ast.choreography.Choreography;

public record ProcedureInfo(
        ProcedureSignature signature,
        Choreography body) {
}
