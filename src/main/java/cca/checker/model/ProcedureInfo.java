package cca.checker.model;

import cca.ast.choreography.Choreography;

public record ProcedureInfo(
        ProcedureSignature signature,
        Choreography body) {
}
