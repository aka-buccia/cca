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
        Program p = parseProgram("");
        assertEquals(Collections.EMPTY_LIST, p.procedures());
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
        Program p = parseProgram(code);
        assertEquals(1, p.procedures().size());

        return p.procedures().getFirst();
    }
}
