package cca.parser;

import cca.FaaSChalCoreLexer;
import cca.FaaSChalCoreParser;
import cca.ast.Program;
import cca.exceptions.CompoundException;

import java.io.File;
import java.io.IOException;

import org.antlr.v4.runtime.*;

public class Parser {

    private static Program parse(String fileName, CharStream input) {
        FaaSChalCoreLexer lexer = new FaaSChalCoreLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FaaSChalCoreParser parser = new FaaSChalCoreParser(tokens);

        parser.removeErrorListeners();
        ParsingErrorListener errorListener = new ParsingErrorListener(fileName);
        parser.addErrorListener(errorListener);

        FaaSChalCoreParser.ProgramContext ctx = parser.program();
        AstOptimizer optimizer = new AstOptimizer();

        if (errorListener.getErrors().isEmpty()) {
            return optimizer.optimise(ctx, fileName);
        } else {
            throw new CompoundException(errorListener.getErrors());
        }

    }

    public static Program parseSourceFile(File file) throws IOException {
        String filename = file.getCanonicalPath();
        CharStream input = CharStreams.fromFileName(filename);
        return parse(filename, input);
    }

    public static Program parseSourceCode(String sourceCode) {
        CharStream input = CharStreams.fromString(sourceCode);
        return parse(null, input);
    }
}
