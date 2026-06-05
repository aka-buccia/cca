package cca.parser;

import cca.ast.Position;
import cca.ast.Program;
import cca.ast.Role;
import cca.ast.Media;
import cca.ast.Label;
import cca.ast.choreography.*;
import cca.ast.expression.*;
import cca.ast.procedure.*;
import cca.ast.instruction.*;
import cca.exceptions.*;

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
    public void parseSimpleProcedureWithOnlyStatefulParams() {
        Procedure procedure = parseProcedure("def ping(a) {0}");

        assertEquals("ping", procedure.id());
        assertEquals(1, procedure.parameterList().size());
        assertInstanceOf(TerminationOrder.TerminationOrderDefault.class, procedure.terminationOrder());

        List<StatefulParameter> statefulParameters = procedure.parameterList().statefulParameters();

        assertEquals(1, statefulParameters.size());
        assertEquals(new Role("a", emptyPosition), statefulParameters.getFirst().parameter());
    }

    @Test
    public void parseSimpleProcedureWithOnlyNonTerminatingParams() {
        Procedure procedure = parseProcedure("def ping(nonterm a) {0}");

        List<NonTerminatingParameter> nonTerminatingParameters = procedure.parameterList().nonTerminatingParameters();

        assertEquals(1, nonTerminatingParameters.size());
        assertEquals(new Role("a", emptyPosition), nonTerminatingParameters.getFirst().parameter());
    }

    @Test
    public void parseSimpleProcedureWithOnlyTerminatingParams() {
        Procedure procedure = parseProcedure("def ping(term a, [b, c]) {0}");

        List<TerminatingParameter> terminatingParameters = procedure.parameterList().terminatingParameters();

        assertEquals(2, terminatingParameters.size());

        assertEquals(new Role("a", emptyPosition), terminatingParameters.getFirst().createdRole());
        assertEquals(new Role("b", emptyPosition), terminatingParameters.getLast().createdRole());
        assertEquals(new Role("c", emptyPosition), terminatingParameters.getLast().creatorRole());
    }

    @Test
    public void parseSimpleProcedureWithTerminationOrder() {
        Procedure procedure = parseProcedure("def ping(term a, b, c): (a <: b, b <: c) {0}");

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
        assertInstanceOf(Expression.class, request.sourceExpression());
        assertEquals(new Media("M", emptyPosition), request.media());
        assertEquals(new Variable("x", emptyPosition), request.targetVariable());
        assertEquals(new Role("b", emptyPosition), request.targetRole());
    }

    @Test
    public void parseSelection() {
        Choreography choreography = parseChoreography("p -> q[L]");

        assertEquals(1, choreography.interactions().size());
        assertInstanceOf(Selection.class, choreography.interactions().getFirst());

        Selection request = (Selection) choreography.interactions().getFirst();

        assertEquals(new Role("p", emptyPosition), request.sourceRole());
        assertEquals(new Label("L", emptyPosition), request.label());
        assertEquals(new Role("q", emptyPosition), request.targetRole());
    }

    @Test
    public void parseAssignment() {
        Choreography choreography = parseChoreography("x@n = sum(2, 3)@n");

        assertEquals(1, choreography.interactions().size());
        assertInstanceOf(Assignment.class, choreography.interactions().getFirst());

        Assignment request = (Assignment) choreography.interactions().getFirst();

        assertEquals(new Role("n", emptyPosition), request.targetRole());
        assertEquals(new Variable("x", emptyPosition), request.variable());
        assertInstanceOf(Expression.class, request.expression());
    }

    @Test
    public void parseRequestResponse() {
        Choreography choreography = parseChoreography("title@a <-M-> x@b |> y@a");

        assertEquals(1, choreography.interactions().size());
        assertInstanceOf(RequestResponse.class, choreography.interactions().getFirst());

        RequestResponse requestResponse = (RequestResponse) choreography.interactions().getFirst();

        assertEquals(new Role("a", emptyPosition), requestResponse.sourceRole());
        assertInstanceOf(Expression.class, requestResponse.sourceExpression());
        assertEquals(new Media("M", emptyPosition), requestResponse.media());
        assertEquals(new Variable("x", emptyPosition), requestResponse.targetVariable());
        assertEquals(new Role("b", emptyPosition), requestResponse.targetRole());
        assertEquals(new Variable("y", emptyPosition), requestResponse.sourceVariable());
    }

    @Test
    public void parseEnd() {
        Choreography choreography = parseChoreography("end f");

        assertEquals(1, choreography.interactions().size());
        assertInstanceOf(End.class, choreography.interactions().getFirst());

        End end = (End) choreography.interactions().getFirst();

        assertEquals(new Role("f", emptyPosition), end.endingRole());
    }

    @Test
    public void parseEndResponse() {
        Choreography choreography = parseChoreography("end \"bye\"@f -> n");

        assertEquals(1, choreography.interactions().size());
        assertInstanceOf(EndResponse.class, choreography.interactions().getFirst());

        EndResponse endResponse = (EndResponse) choreography.interactions().getFirst();

        assertInstanceOf(Expression.class, endResponse.expression());
        assertEquals(new Role("f", emptyPosition), endResponse.endingRole());
        assertEquals(new Role("n", emptyPosition), endResponse.targetRole());
    }

    @Test
    public void parseConditionalWithoutProsecution() {
        Choreography choreography = parseChoreography("if received()@n then {n->q[OK]} else {0}");

        assertEquals(1, choreography.interactions().size());
        assertInstanceOf(Conditional.class, choreography.interactions().getFirst());

        Conditional conditional = (Conditional) choreography.interactions().getFirst();

        assertInstanceOf(Expression.class, conditional.condition());
        assertEquals(new Role("n", emptyPosition), conditional.targetRole());
        assertInstanceOf(Choreography.class, conditional.ifBranch());
        assertInstanceOf(Choreography.class, conditional.elseBranch());
    }

    @Test
    public void parseConditionalWithoutElse() {
        Choreography choreography = parseChoreography("if received()@n then {n->q[OK]}");

        assertEquals(1, choreography.interactions().size());
        assertInstanceOf(Conditional.class, choreography.interactions().getFirst());

        Conditional conditional = (Conditional) choreography.interactions().getFirst();

        assertInstanceOf(Expression.class, conditional.condition());
        assertInstanceOf(Terminated.TerminatedOmitted.class, conditional.elseBranch().termination());
    }

    @Test
    public void parseConditionalWithProsecution() {
        Choreography choreography = parseChoreography("if received()@n then {n->q[L]} else {0}; x@n = sum(2, 3)@n");

        assertEquals(2, choreography.interactions().size());
        assertInstanceOf(Conditional.class, choreography.interactions().getFirst());
        assertInstanceOf(Assignment.class, choreography.interactions().getLast());
    }

    @Test
    public void parseProcedureCallWithoutParamsShouldRaiseAnException() {
        assertThrows(FaaSChalCoreException.class, () -> parseChoreography("X()"));
    }

    @Test
    public void parseProcedureCallWithAllParams() {
        Choreography choreography = parseChoreography("X(a, b, nonterm c, term [g, f], e)");

        assertEquals(1, choreography.interactions().size());
        assertInstanceOf(ProcedureCall.class, choreography.interactions().getFirst());

        ProcedureCall procedureCall = (ProcedureCall) choreography.interactions().getFirst();

        assertEquals("X", procedureCall.name());
        assertEquals(5, procedureCall.parameterList().size());
    }

    // HELPERS

    private OrderingCouple createOrderingCouple(String leftRoleName, String rightRoleName) {
        Role leftRole = new Role(leftRoleName, emptyPosition);
        Role rightRole = new Role(rightRoleName, emptyPosition);

        return new OrderingCouple(leftRole, rightRole, emptyPosition);
    }

    private Program parseProgram(String code) {
        return Parser.parseSourceCode(code);
    }

    // Helper: parse a single procedure
    private Procedure parseProcedure(String code) {
        Program program = parseProgram(code);
        assertEquals(1, program.procedures().size());

        return program.procedures().getFirst();
    }

    private Choreography parseChoreography(String code) {
        String procedureCode = "def procedureWrapper(a) {" + code + "}"; // wraps choreography code inside a procedure
        Procedure procedure = parseProcedure(procedureCode);

        return procedure.choreography();

    }
}
