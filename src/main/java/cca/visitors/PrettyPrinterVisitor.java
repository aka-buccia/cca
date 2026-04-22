package cca.visitors;

import cca.Program;
import cca.procedure.*;

public class PrettyPrinterVisitor extends AbstractVisitor<String> {

    private static final String DEF = "def";
    private static final String ARROW = "->";
    private static final String END = "end";
    private static final String SEMICOLON = ";";
    private static final String TAB = "\t";
    private static final String NEWLINE = "\n";
    private static final String _2NEWLINE = NEWLINE + NEWLINE;;
    private static final String COMMA = ",";
    private static final String SPACED_COMMA = COMMA + " ";

}
