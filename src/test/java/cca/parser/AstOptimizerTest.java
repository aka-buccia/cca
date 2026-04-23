package cca.parser;

import cca.FaaSChalCoreLexer;
import cca.FaaSChalCoreParser;
import cca.Program;
import cca.procedure.*;
import org.junit.jupiter.api.Test;
import cca.optimizer.AstOptimizer;
import org.antlr.v4.runtime.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

public class AstOptimizerTest {

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

    // Helpers

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
}
