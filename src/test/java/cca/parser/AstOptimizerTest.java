package cca.parser;

import cca.FaaSChalCoreLexer;
import cca.FaaSChalCoreParser;
import cca.Position;
import cca.Program;
import cca.Role;
import cca.Media;
import cca.Label;
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
    public void parseCommunication() {
        Choreography choreography = parseChoreography("42@a -> x@b");

        assertEquals(1, choreography.interactions().size());
        assertInstanceOf(Communication.class, choreography.interactions().getFirst());

        Communication communication = (Communication) choreography.interactions().getFirst();

        assertEquals(new Role("a", emptyPosition), communication.leftRole());
        assertEquals(new Role("b", emptyPosition), communication.rightRole());
    }

    @Test
    public void parseConstantInteger() {
        Choreography choreography = parseChoreography("42@a -> x@b");
        Communication communication = (Communication) choreography.interactions().getFirst();

        assertInstanceOf(Constant.ConstantInt.class, communication.expression());

        Constant<Integer> constant = (Constant.ConstantInt) communication.expression();

        assertEquals(42, constant.value());
    }

    @Test
    public void parseConstantString() {
        Choreography choreography = parseChoreography("\"hi\"@a -> x@b");
        Communication communication = (Communication) choreography.interactions().getFirst();

        assertInstanceOf(Constant.ConstantString.class, communication.expression());

        Constant<String> constant = (Constant.ConstantString) communication.expression();

        assertEquals("hi", constant.value());
    }

    @Test
    public void parseVariable() {
        Choreography choreography = parseChoreography("42@a -> x@b");
        Communication communication = (Communication) choreography.interactions().getFirst();

        assertInstanceOf(Variable.class, communication.variable());

        Variable variable = communication.variable();

        assertEquals("x", variable.id());
    }

    @Test
    public void parseLocalFunctionCallWithoutParameters() {
        Choreography choreography = parseChoreography("order()@a -> x@b");
        Communication communication = (Communication) choreography.interactions().getFirst();

        assertInstanceOf(LocalFunction.class, communication.expression());

        LocalFunction function = (LocalFunction) communication.expression();

        assertEquals("order", function.id());
        assertEquals(Collections.EMPTY_LIST, function.parameters());
    }

    @Test
    public void parseLocalFunctionCallWithFunctionParameters() {
        Choreography choreography = parseChoreography("order(\"first\", 2)@a -> x@b");
        Communication communication = (Communication) choreography.interactions().getFirst();
        LocalFunction function = (LocalFunction) communication.expression();

        assertInstanceOf(List.class, function.parameters());

        List<Expression> parameters = function.parameters();

        assertEquals("first", ((Constant<String>) parameters.get(0)).value());
        assertEquals(2, ((Constant<Integer>) parameters.get(1)).value());

    }

    @Test
    public void parseRequest() {
        Choreography choreography = parseChoreography("title@a -M-> x@b");

        assertEquals(1, choreography.interactions().size());
        assertInstanceOf(Request.class, choreography.interactions().getFirst());

        Request request = (Request) choreography.interactions().getFirst();

        assertEquals(new Role("a", emptyPosition), request.sourceRole());
        assertEquals(new Media("M", emptyPosition), request.media());
        assertEquals(new Role("b", emptyPosition), request.targetRole());
    }

    @Test
    public void parseSelection() {
        Choreography choreography = parseChoreography("p -> q[L]");

        assertEquals(1, choreography.interactions().size());
        assertInstanceOf(Request.class, choreography.interactions().getFirst());

        Selection request = (Selection) choreography.interactions().getFirst();

        assertEquals(new Role("p", emptyPosition), request.sourceRole());
        assertEquals(new Label("L", emptyPosition), request.label());
        assertEquals(new Role("q", emptyPosition), request.targetRole());
    }

    // HELPERS

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
