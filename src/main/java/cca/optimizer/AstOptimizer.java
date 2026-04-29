package cca.optimizer;

import cca.FaaSChalCoreBaseVisitor;
import cca.FaaSChalCoreParser;
import cca.Node;
import cca.Position;
import cca.Program;
import cca.Role;
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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class AstOptimizer extends FaaSChalCoreBaseVisitor<Node> {

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
        } else {
            throw new SyntaxException(getPosition(ctx.communication()),
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

    // ----- UTILITIES

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
