package cca.visitors;

import cca.Program;
import cca.procedure.Procedure;

public interface VisitorInterface<R> {

    R visit(Program n);

    R visit(Procedure n);

}
