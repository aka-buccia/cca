package cca.parser;

import cca.FaaSChalCoreLexer;
import cca.FaaSChalCoreParser;
import cca.Position;
import cca.Program;
import cca.Role;
import cca.procedure.*;
import cca.choreography.*;
import cca.interaction.*;
import cca.expression.*;
import cca.optimizer.AstOptimizer;

import org.antlr.v4.runtime.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Collections;

public class AstOptimizerTest {

    private static final Position emptyPosition = new Position(null, -1, -1); // position placeholder for testing node

    @Test
    public void parseEmptyProgramShouldReturnEmptyProcedureList() {
        Program program = parseProgram("");
        assertEquals(Collections.EMPTY_LIST, program.procedures());
    }

    @Test
    public void parseSimpleProcedureWithoutParams() {
        Procedure procedure = parseProcedure("def ping() { 0 }");

        assertEquals("ping", procedure.id());
        assertEquals(Collections.EMPTY_LIST, procedure.parameters());
        assertInstanceOf(TerminationOrder.TerminationOrderDefault.class, procedure.terminationOrder());
        assertNotNull(procedure.choreography());

    }

    @Test
    public void parseSimpleProcedureWithTerminationOrder() {
        Procedure procedure = parseProcedure("def ping(): (a <: b, b <: c) {0}");

        assertInstanceOf(List.class, procedure.terminationOrder().elements());
        OrderingCouple firstCouple = procedure.terminationOrder().elements().get(0);
        OrderingCouple secondCouple = procedure.terminationOrder().elements().get(1);

        assertEquals(createOrderingCouple("a", "b"), firstCouple);
        assertEquals(createOrderingCouple("b", "c"), secondCouple);

    }

    @Test
    public void parseChoreographyWithOnlyTermination() {
        Choreography choreography = parseChoreography("0");
        assertInstanceOf(Terminated.class, choreography.termination());
    }

    @Test
    public void parseChoreographyWithoutTermination() {
        Choreography choreography = parseChoreography("42@a -> x@b");
        assertInstanceOf(Terminated.TerminatedOmitted.class, choreography.termination());
    }

    @Test
    public void parseSimpleCommunication() {
        Choreography choreography = parseChoreography("42@a -> x@b");

        assertEquals(1, choreography.interactions().size());
        assertInstanceOf(Communication.class, choreography.interactions().getFirst());

        Communication communication = (Communication) choreography.interactions().getFirst();

        assertEquals(new Role("a", emptyPosition), communication.leftRole());
        assertEquals(new Role("b", emptyPosition), communication.rightRole());

    }

    @Test
    public void parseConstantExpression() {
        Choreography choreography = parseChoreography("42@a -> x@b");
        Communication communication = (Communication) choreography.interactions().getFirst();

        assertEquals(new Constant("a", emptyPosition), communication.leftRole());

    }

    // Helpers

    private OrderingCouple createOrderingCouple(String leftRoleName, String rightRoleName) {
        Role leftRole = new Role(leftRoleName, emptyPosition);
        Role rightRole = new Role(rightRoleName, emptyPosition);

        return new OrderingCouple(leftRole, rightRole, emptyPosition());
    }

    private Position emptyPosition() {
        return new Position(null, -1, -1);
    }

    private Program parseProgram(String code) {
        CharStream input = CharStreams.fromString(code);
        FaaSChalCoreLexer lexer = new FaaSChalCoreLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FaaSChalCoreParser parser = new FaaSChalCoreParser(tokens);

        FaaSChalCoreParser.ProgramContext ctx = parser.program();
        AstOptimizer optimizer = new AstOptimizer();
        return optimizer.visitProgram(ctx);
    }

    // Helper: parsa una singola procedure
    private Procedure parseProcedure(String code) {
        Program program = parseProgram(code);
        assertEquals(1, program.procedures().size());

        return program.procedures().getFirst();
    }

    private Choreography parseChoreography(String code) {
        String procedureCode = "def procedureWrapper() {" + code + "}"; // wraps choreography code inside a procedure
        Procedure procedure = parseProcedure(procedureCode);

        return procedure.choreography();

    }
}
