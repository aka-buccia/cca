package cca.visitors;

import cca.Program;
import cca.Role;
import cca.expression.*;
import cca.choreography.*;
import cca.interaction.*;
import cca.procedure.*;

public interface VisitorInterface<R> {

    R visit(Program n);

    R visit(Procedure n);

    R visit(TerminationOrder n);

    R visit(OrderingCouple n);

    R visit(Choreography n);

    R visit(Terminated n);

    R visit(Communication n);

    R visit(Constant<?> n);

    R visit(LocalFunction n);

    R visit(Role n);

    R visit(Variable n);

}
