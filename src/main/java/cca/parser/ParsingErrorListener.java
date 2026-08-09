package cca.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import cca.ast.Position;
import cca.exceptions.SyntaxException;

public class ParsingErrorListener extends BaseErrorListener {

    private final String file;
    private final List<SyntaxException> errors;

    public ParsingErrorListener(String file) {
        this.file = file;
        this.errors = new ArrayList<>();
    }

    public List<SyntaxException> getErrors() {
        return this.errors;
    }

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e) {

        String file = recognizer.getInputStream().getSourceName().equals("<unknown>")
                ? this.file
                : recognizer.getInputStream().getSourceName();
        errors.add(new SyntaxException(new Position(file, line, charPositionInLine + 1), msg));
    }
}
