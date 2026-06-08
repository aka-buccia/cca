package cca.ast.visitors;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cca.ast.Name;
import cca.ast.Node;
import cca.ast.Program;
import cca.ast.Role;
import cca.ast.choreography.*;
import cca.ast.procedure.*;

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
    private static final String AT = "@";
    private static final String TERMINATION = "0";

    @Override
    public String visit(Program n) {
        return visitAndCollect(n.procedures(), NEWLINE);
    }

    @Override
    public String visit(Procedure n) {
        StringBuilder sb = new StringBuilder();

        sb.append(DEF).append(" ");
        sb.append(visit(n.name()));

        // parameters
        sb.append("(");
        sb.append(visit(n.parameterList()));
        sb.append(")");

        // termination order
        if (!(n.terminationOrder() instanceof TerminationOrder.TerminationOrderDefault)) {
            sb.append(":");
            sb.append("(");
            sb.append(visit(n.terminationOrder()));
            sb.append(")").append(" ");
        }

        // body
        sb.append("{").append(NEWLINE);
        sb.append(visit(n.choreography()));
        sb.append("}").append(NEWLINE);

        return sb.toString();
    }

    @Override
    public String visit(ProcedureParameterList n) {
        StringBuilder sb = new StringBuilder();

        if (!n.statefulParameters().isEmpty()) {
            sb.append("stateful").append(" ");
            sb.append(visitAndCollect(n.statefulParameters(), SPACED_COMMA));
            sb.append(SPACED_COMMA);
        }

        if (!n.nonTerminatingParameters().isEmpty()) {
            sb.append("non term").append(" ");
            sb.append(visitAndCollect(n.nonTerminatingParameters(), SPACED_COMMA));
            sb.append(SPACED_COMMA);
        }

        if (!n.terminatingParameters().isEmpty()) {
            sb.append("term").append(" ");
            sb.append(visitAndCollect(n.terminatingParameters(), SPACED_COMMA));
        }

        return sb.toString();
    }

    @Override
    public String visit(StatefulParameter n) {
        return visit(n.parameter());
    }

    @Override
    public String visit(NonTerminatingParameter n) {
        return visit(n.parameter());
    }

    @Override
    public String visit(TerminatingParameter n) {
        StringBuilder sb = new StringBuilder();

        sb.append(visit(n.createdRole()));

        if (n.creatorRole() == null) {
            sb.append("0");
        } else {
            sb.append(visit(n.creatorRole()));
        }

        return sb.toString();
    }

    @Override
    public String visit(TerminationOrder n) {
        return visitAndCollect(n.elements(), SPACED_COMMA);
    }

    @Override
    public String visit(OrderingCouple n) {
        StringBuilder sb = new StringBuilder();

        sb.append(visit(n.left()));
        sb.append("<:");
        sb.append(visit(n.right()));

        return sb.toString();
    }

    @Override
    public String visit(Terminated n) {
        return TERMINATION;
    }

    @Override
    public String visit(Role n) {
        return visit(n.name());
    }

    @Override
    public String visit(Name n) {
        return n.id();
    }

    // HELPERS

    protected final <T extends Node> String visitAndCollect(List<T> list, String delimiter) {
        return list.stream().filter(Objects::nonNull).map(e -> e.accept(this)).collect(
                Collectors.joining(delimiter));
    }

    protected final <T extends Node> String visitAndCollect(
            List<T> list, String delimiter, String closure) {
        return visitAndCollect(list, delimiter) + (list.isEmpty() ? " " : closure);
    }

    protected final String indent(String s) {
        return Stream.of(s.split(NEWLINE)).map(l -> TAB + l).collect(
                Collectors.joining(NEWLINE));
    }
}
