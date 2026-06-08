package cca.ast.visitors;

import cca.ast.Program;
import cca.ast.Role;
import cca.ast.Media;
import cca.ast.Label;
import cca.ast.Name;
import cca.ast.choreography.*;
import cca.ast.expression.*;
import cca.ast.instruction.*;
import cca.ast.procedure.*;

public interface VisitorInterface<R> {

    R visit(Program n);

    R visit(Procedure n);

    R visit(TerminationOrder n);

    R visit(OrderingCouple n);

    R visit(Choreography n);

    R visit(Terminated n);

    R visit(Instruction n);

    R visit(Communication n);

    R visit(Request n);

    R visit(Selection n);

    R visit(Assignment n);

    R visit(End n);

    R visit(EndResponse n);

    R visit(Conditional n);

    R visit(ProcedureCall n);

    R visit(ProcedureParameterList n);

    R visit(StatefulParameter n);

    R visit(NonTerminatingParameter n);

    R visit(TerminatingParameter n);

    R visit(Constant<?> n);

    R visit(LocalFunction n);

    R visit(Role n);

    R visit(Media n);

    R visit(Variable n);

    R visit(Label n);

    R visit(Name n);
}
