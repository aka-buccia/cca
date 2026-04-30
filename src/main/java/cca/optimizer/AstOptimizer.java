package cca.optimizer;

import cca.FaaSChalCoreVisitor;
import cca.FaaSChalCoreParser;
import cca.FaaSChalCoreParser.NonterminatingParametersContext;
import cca.FaaSChalCoreParser.ProcedureCallContext;
import cca.FaaSChalCoreParser.ProcedureNameContext;
import cca.FaaSChalCoreParser.ProcedureParametersContext;
import cca.FaaSChalCoreParser.StatefulParametersContext;
import cca.FaaSChalCoreParser.TerminatingParametersContext;
import cca.FaaSChalCoreParser.TerminatingTermContext;
import cca.Node;
import cca.Position;
import cca.Program;
import cca.Role;
import cca.Media;
import cca.Label;
import cca.procedure.*;
import cca.choreography.*;
import cca.expression.*;
import cca.interaction.*;
import cca.exceptions.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.text.ParseException;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class AstOptimizer implements FaaSChalCoreVisitor {

    private String file = "";

    public Program optimise(FaaSChalCoreParser.ProgramContext ctx, String file) {
        this.file = file;
        return visitProgram(ctx);
    }

    @Override
    public Program visitProgram(FaaSChalCoreParser.ProgramContext ctx) {
        List<Procedure> procedures = ctx.procedure().stream().map(this::visitProcedure).collect(Collectors.toList());

        return new Program(procedures, getPosition(ctx));

    }

    @Override
    public Procedure visitProcedure(FaaSChalCoreParser.ProcedureContext ctx) {
        String name = ctx.procedureName().getText();

        List<ProcedureParameter> parameters = Collections.emptyList(); // TODO implment ProcedureParameters
        TerminationOrder terminationOrder = isPresent(ctx.terminationOrder())
                ? visitTerminationOrder(ctx.terminationOrder())
                : new TerminationOrder.TerminationOrderDefault(getPosition(ctx)); // TODO needs an accurate position
        Choreography choreography = visitChoreography(ctx.choreography());

        return new Procedure(name, parameters, terminationOrder, choreography, getPosition(ctx));

    }

    @Override
    public TerminationOrder visitTerminationOrder(FaaSChalCoreParser.TerminationOrderContext ctx) {

        List<OrderingCouple> elements = ctx.orderingCouple().stream().map(this::visitOrderingCouple)
                .collect(Collectors.toList());

        return new TerminationOrder(elements, getPosition(ctx));

    }

    @Override
    public OrderingCouple visitOrderingCouple(FaaSChalCoreParser.OrderingCoupleContext ctx) {

        Role left = visitRole(ctx.role().getFirst());
        Role right = visitRole(ctx.role().getLast());

        return new OrderingCouple(left, right, getPosition(ctx));
    }

    @Override
    public Choreography visitChoreography(FaaSChalCoreParser.ChoreographyContext ctx) {

        List<Interaction> interactions = ctx.interaction().stream().map(this::visitInteraction)
                .collect(Collectors.toList());
        Terminated termination = isPresent(ctx.terminated()) ? visitTerminated(ctx.terminated())
                : new Terminated.TerminatedOmitted(getPosition(ctx.getStop()));

        return new Choreography(interactions, termination, getPosition(ctx));
    }

    @Override
    public Terminated visitTerminated(FaaSChalCoreParser.TerminatedContext ctx) {
        return new Terminated(getPosition(ctx));
    }

    @Override
    public Role visitRole(FaaSChalCoreParser.RoleContext ctx) {

        return new Role(ctx.ID().getText(), getPosition(ctx));
    }

    @Override
    public Variable visitVariable(FaaSChalCoreParser.VariableContext ctx) {
        return new Variable(ctx.ID().getText(), getPosition(ctx));
    }

    @Override
    public Constant<?> visitConstant(FaaSChalCoreParser.ConstantContext ctx) {
        if (isPresent(ctx.INT())) {
            int value = Integer.parseInt(ctx.INT().getText());
            return new Constant.ConstantInt(value, getPosition(ctx));
        } else if (isPresent(ctx.STRING())) {
            String value = ctx.STRING().getText();
            // Rimuovi le virgolette se presenti
            value = value.substring(1, value.length() - 1);
            return new Constant.ConstantString(value, getPosition(ctx));
        } else {
            throw new SyntaxException(getPosition(ctx), "Unrecognized constant: '" + ctx.getText() + "'");
        }
    }

    @Override
    public Interaction visitInteraction(FaaSChalCoreParser.InteractionContext ctx) {

        if (isPresent(ctx.communication())) {
            return visitCommunication(ctx.communication());
        } else if (isPresent(ctx.request())) {
            return visitRequest(ctx.request());
        } else if (isPresent(ctx.selection())) {
            return visitSelection(ctx.selection());
        } else if (isPresent(ctx.assignment())) {
            return visitAssignment(ctx.assignment());
        } else if (isPresent(ctx.requestResponse())) {
            return visitRequestResponse(ctx.requestResponse());
        } else if (isPresent(ctx.end())) {
            return visitEnd(ctx.end());
        } else if (isPresent(ctx.endResponse())) {
            return visitEndResponse(ctx.endResponse());
        } else if (isPresent(ctx.conditional())) {
            return visitConditional(ctx.conditional());
        } else if (isPresent(ctx.procedureCall())) {
            return visitProcedureCall(ctx.procedureCall());
        } else {
            throw new SyntaxException(getPosition(ctx),
                    "Unrecognized interaction: '" + ctx.getText() + "'");
        }

    }

    @Override
    public Communication visitCommunication(FaaSChalCoreParser.CommunicationContext ctx) {

        Expression expression = visitExpression(ctx.expression());
        Role leftRole = visitRole(ctx.role(0));
        Variable variable = visitVariable(ctx.variable());
        Role rightRole = visitRole(ctx.role(1));

        return new Communication(expression, leftRole, variable, rightRole, getPosition(ctx));
    }

    @Override
    public Expression visitExpression(FaaSChalCoreParser.ExpressionContext ctx) {

        if (isPresent(ctx.constant())) {
            return visitConstant(ctx.constant());
        } else if (isPresent(ctx.variable())) {
            return visitVariable(ctx.variable());
        } else if (isPresent(ctx.function())) {
            return visitFunction(ctx.function());
        } else {
            throw new SyntaxException(getPosition(ctx), "Unrecognized expression: '" + ctx.getText() + "'");
        }
    }

    @Override
    public LocalFunction visitFunction(FaaSChalCoreParser.FunctionContext ctx) {

        String id = ctx.ID().getText();
        List<Expression> parameters = ifPresent(ctx.functionParameters()).applyOrElse(this::visitFunctionParameters,
                Collections::emptyList);

        return new LocalFunction(id, parameters, getPosition(ctx));
    }

    @Override
    public List<Expression> visitFunctionParameters(FaaSChalCoreParser.FunctionParametersContext ctx) {

        List<Expression> parameters = ctx.expression().stream().map(this::visitExpression).collect(Collectors.toList());

        return parameters;
    }

    @Override
    public Request visitRequest(FaaSChalCoreParser.RequestContext ctx) {

        Expression sourceExpression = visitExpression(ctx.expression());
        Role sourceRole = visitRole(ctx.role(0));
        Media media = visitMedia(ctx.media());
        Variable targetVariable = visitVariable(ctx.variable());
        Role targetRole = visitRole(ctx.role(1));

        return new Request(sourceExpression, sourceRole, media, targetVariable, targetRole, getPosition(ctx));

    }

    @Override
    public RequestResponse visitRequestResponse(FaaSChalCoreParser.RequestResponseContext ctx) {

        if (!checkRoleMatching(ctx.role(0), ctx.role(2))) {
            throw new SyntaxException(getPosition(ctx),
                    "Request-response role mismatch: '" + ctx.getText()
                            + "'. The first and the last role must match");
        }

        Expression sourceExpression = visitExpression(ctx.expression());
        Role sourceRole = visitRole(ctx.role(0));
        Media media = visitMedia(ctx.media());
        Variable targetVariable = visitVariable(ctx.variable(0));
        Role targetRole = visitRole(ctx.role(1));
        Variable sourceVariable = visitVariable(ctx.variable(1));

        return new RequestResponse(sourceExpression, sourceRole, media, targetVariable, targetRole, sourceVariable,
                getPosition(ctx));

    }

    @Override
    public End visitEnd(FaaSChalCoreParser.EndContext ctx) {
        Role endingRole = visitRole(ctx.role());
        return new End(endingRole, getPosition(ctx));
    }

    @Override
    public Media visitMedia(FaaSChalCoreParser.MediaContext ctx) {
        String id = ctx.ID().getText();
        return new Media(id, getPosition(ctx));
    }

    @Override
    public Selection visitSelection(FaaSChalCoreParser.SelectionContext ctx) {
        Role sourceRole = visitRole(ctx.role(0));
        Role targetRole = visitRole(ctx.role(1));
        Label label = visitLabel(ctx.label());

        return new Selection(sourceRole, targetRole, label, getPosition(ctx));
    }

    @Override
    public Label visitLabel(FaaSChalCoreParser.LabelContext ctx) {
        String id = ctx.ID().getText();
        return new Label(id, getPosition(ctx));
    }

    @Override
    public Assignment visitAssignment(FaaSChalCoreParser.AssignmentContext ctx) {

        if (!checkRoleMatching(ctx.role(0), ctx.role(1))) {
            throw new SyntaxException(getPosition(ctx),
                    "Assignment role mismatch: '" + ctx.getText() + "'. Left and right sides must have the same role");
        }

        Variable variable = visitVariable(ctx.variable());
        Role targetRole = visitRole(ctx.role(0));
        Expression expression = visitExpression(ctx.expression());

        return new Assignment(variable, targetRole, expression, getPosition(ctx));
    }

    @Override
    public EndResponse visitEndResponse(FaaSChalCoreParser.EndResponseContext ctx) {
        Expression expression = visitExpression(ctx.expression());
        Role endingRole = visitRole(ctx.role(0));
        Role targetRole = visitRole(ctx.role(1));

        return new EndResponse(expression, endingRole, targetRole, getPosition(ctx));
    }

    @Override
    public Conditional visitConditional(FaaSChalCoreParser.ConditionalContext ctx) {
        Expression condition = visitExpression(ctx.expression());
        Role targetRole = visitRole(ctx.role());
        Choreography ifBranch = visitChoreography(ctx.choreography(0));
        Choreography elseBranch = visitChoreography(ctx.choreography(1));

        return new Conditional(condition, targetRole, ifBranch, elseBranch, getPosition(ctx));
    }

    @Override
    public ProcedureCall visitProcedureCall(FaaSChalCoreParser.ProcedureCallContext ctx) {

        String name = visitProcedureName(ctx.procedureName());
        List<ProcedureParameter> parameters = Collections.emptyList();

        return new ProcedureCall(name, parameters, getPosition(ctx));
    }

    @Override
    public String visitProcedureName(FaaSChalCoreParser.ProcedureNameContext ctx) {

        return ctx.ID().getText();
    }

    @Override
    public Object visitErrorNode(ErrorNode errorNode) {
        new ParseException("Parsing Error " + errorNode.getText(), errorNode.getSourceInterval().a).printStackTrace();
        return null;
    }

    // Interface methods not implemented

    @Override
    public Object visitProcedureParameters(ProcedureParametersContext ctx) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitNonterminatingParameters(NonterminatingParametersContext ctx) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitStatefulParameters(StatefulParametersContext ctx) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitTerminatingParameters(TerminatingParametersContext ctx) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitTerminatingTerm(TerminatingTermContext ctx) {
        // TODO Auto-generated method stub
        return null;
    }

    // Interface methods that shouldn't be used by AstOptimizer

    @Override
    public Node visit(ParseTree parseTree) {
        throw new UnsupportedOperationException("The AstOptimizer should not visit the ParseTree");
    }

    @Override
    public Object visitChildren(RuleNode ruleNode) {
        throw new UnsupportedOperationException("The AstOptimizer should not visit nodes as generic RuleNodes");
    }

    @Override
    public Object visitTerminal(TerminalNode terminalNode) {
        throw new UnsupportedOperationException("The AstOptimizer should not visit TerminalNodes");
    }

    // ----- UTILITIES

    private boolean checkRoleMatching(FaaSChalCoreParser.RoleContext leftRole,
            FaaSChalCoreParser.RoleContext rightRole) {
        return leftRole.ID().getText().equals(rightRole.ID().getText());
    }

    // Method for extracting token position
    private Position getPosition(Token t) {
        return new Position(this.file, t.getLine(), t.getCharPositionInLine());
    }

    private Position getPosition(ParserRuleContext c) {
        return getPosition(c.getStart());
    }

    private boolean isPresent(ParserRuleContext p) {
        return p != null && !p.isEmpty();
    }

    private boolean isPresent(TerminalNode p) {
        return p != null;
    }

    // Personalized Optional wrapper
    private <T> IfPresent<T> ifPresent(T o) {
        return new IfPresent<>(o);
    }

    private static class IfPresent<T> {

        private final Optional<T> o;

        IfPresent(T o) {
            this.o = Optional.ofNullable(o);
        }

        <R> Optional<R> apply(Function<T, R> f) {
            if (f == null) {
                throw new RuntimeException("Application function must be not null");
            }
            return o.map(f);
        }

        <R> R applyOrElse(Function<T, R> f, Supplier<R> e) {
            if (f == null || e == null) {
                throw new RuntimeException(
                        "Both application and alternative functions must be not null");
            }
            return o.map(f).orElseGet(e);
        }

    }
}
