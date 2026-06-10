package cca.ast.visitors;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cca.ast.Label;
import cca.ast.Media;
import cca.ast.Name;
import cca.ast.Node;
import cca.ast.Program;
import cca.ast.Role;
import cca.ast.choreography.*;
import cca.ast.expression.*;
import cca.ast.instruction.*;
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
        return visitAndCollect(n.procedures(), _2NEWLINE);
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
            sb.append(")");
        }

        // body
        sb.append(" ").append("{").append(NEWLINE);
        sb.append(indent(visit(n.choreography())));
        sb.append(NEWLINE).append("}");

        return sb.toString();
    }

    @Override
    public String visit(ProcedureParameterList n) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        if (!n.statefulParameters().isEmpty()) {
            sb.append(visitAndCollect(n.statefulParameters(), SPACED_COMMA));
            first = false;
        }

        if (!n.nonTerminatingParameters().isEmpty()) {
            if (!first)
                sb.append(SPACED_COMMA);
            sb.append("non term").append(" ");
            sb.append(visitAndCollect(n.nonTerminatingParameters(), SPACED_COMMA));
            first = false;
        }

        if (!n.terminatingParameters().isEmpty()) {
            if (!first)
                sb.append(SPACED_COMMA);
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

        sb.append("[");
        sb.append(visit(n.createdRole()));
        sb.append(SPACED_COMMA);

        if (n.creatorRole() == null) {
            sb.append("0");
        } else {
            sb.append(visit(n.creatorRole()));
        }

        sb.append("]");

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
    public String visit(Choreography n) {
        StringBuilder sb = new StringBuilder();

        sb.append(visitAndCollect(n.instructions(), SEMICOLON + NEWLINE, SEMICOLON + NEWLINE));
        sb.append(visit(n.termination()));

        return sb.toString();
    }

    @Override
    public String visit(Instruction n) {
        return n.accept(this);
    }

    @Override
    public String visit(Communication n) {
        StringBuilder sb = new StringBuilder();

        sb.append(visit(n.expression()));
        sb.append(AT);
        sb.append(visit(n.leftRole()));
        sb.append(" ").append(ARROW).append(" ");
        sb.append(visit(n.variable()));
        sb.append(AT);
        sb.append(visit(n.rightRole()));

        return sb.toString();
    }

    @Override
    public String visit(Request n) {
        StringBuilder sb = new StringBuilder();

        sb.append(visit(n.sourceExpression()));
        sb.append(AT);
        sb.append(visit(n.sourceRole()));

        sb.append(" ");
        sb.append("-");
        sb.append(visit(n.media()));
        sb.append("->");
        sb.append(" ");

        sb.append(visit(n.targetVariable()));
        sb.append(AT);
        sb.append(visit(n.targetRole()));

        return sb.toString();
    }

    @Override
    public String visit(RequestResponse n) {
        StringBuilder sb = new StringBuilder();

        sb.append(visit(n.sourceExpression()));
        sb.append(AT);
        sb.append(visit(n.sourceRole()));

        sb.append(" ");
        sb.append("<-");
        sb.append(visit(n.media()));
        sb.append("->");
        sb.append(" ");

        sb.append(visit(n.targetVariable()));
        sb.append(AT);
        sb.append(visit(n.targetRole()));

        sb.append(" ").append("|>").append(" ");
        sb.append(visit(n.sourceVariable()));
        sb.append(AT);
        sb.append(visit(n.sourceRole()));

        return sb.toString();
    }

    @Override
    public String visit(Selection n) {
        StringBuilder sb = new StringBuilder();

        sb.append(visit(n.sourceRole()));
        sb.append(" ").append(ARROW).append(" ");
        sb.append(visit(n.targetRole()));
        sb.append("[");
        sb.append(visit(n.label()));
        sb.append("]");

        return sb.toString();
    }

    @Override
    public String visit(Assignment n) {
        StringBuilder sb = new StringBuilder();

        sb.append(visit(n.variable()));
        sb.append(AT);
        sb.append(visit(n.targetRole()));

        sb.append(" ").append("=").append(" ");

        sb.append(visit(n.expression()));
        sb.append(AT);
        sb.append(visit(n.targetRole()));

        return sb.toString();
    }

    @Override
    public String visit(End n) {
        StringBuilder sb = new StringBuilder();

        sb.append(END).append(" ");
        sb.append(visit(n.endingRole()));

        return sb.toString();
    }

    @Override
    public String visit(EndResponse n) {
        StringBuilder sb = new StringBuilder();

        sb.append(END).append(" ");
        sb.append(visit(n.expression()));
        sb.append(AT);
        sb.append(visit(n.endingRole()));

        sb.append(" ").append(ARROW).append(" ");
        sb.append(visit(n.targetRole()));

        return sb.toString();
    }

    @Override
    public String visit(Conditional n) {
        StringBuilder sb = new StringBuilder();

        sb.append("if").append(" ");

        sb.append(visit(n.condition()));
        sb.append(AT);
        sb.append(visit(n.targetRole()));

        sb.append(" ").append("then").append(" ").append("{");

        sb.append(NEWLINE);
        sb.append(indent(visit(n.ifBranch())));
        sb.append(NEWLINE);

        sb.append("}").append(" ").append("else").append(" ").append("{");

        sb.append(NEWLINE);
        sb.append(indent(visit(n.elseBranch())));
        sb.append(NEWLINE);

        sb.append("}");

        return sb.toString();
    }

    @Override
    public String visit(ProcedureCall n) {
        StringBuilder sb = new StringBuilder();

        sb.append(visit(n.name()));
        sb.append("(");
        sb.append(visit(n.parameterList()));
        sb.append(")");

        return sb.toString();
    }

    @Override
    public String visit(Expression n) {
        return n.accept(this);
    }

    @Override
    public String visit(Terminated n) {
        return TERMINATION;
    }

    @Override
    public String visit(Constant<?> n) {
        return n.value().toString();
    }

    @Override
    public String visit(LocalFunction n) {
        StringBuilder sb = new StringBuilder();

        sb.append(visit(n.name()));
        sb.append("(");
        sb.append(visitAndCollect(n.parameters(), SPACED_COMMA));
        sb.append(")");

        return sb.toString();
    }

    @Override
    public String visit(Role n) {
        return visit(n.name());
    }

    @Override
    public String visit(Media n) {
        return visit(n.name());
    }

    @Override
    public String visit(Variable n) {
        return visit(n.name());
    }

    @Override
    public String visit(Label n) {
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
