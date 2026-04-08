package cca.visitors;

import cca.Program;
import cca.procedure.*;

public interface VisitorInterface<R> {

    R visit(Program n);

    R visit(Procedure n);

    R visit(TerminationOrder n);

}
