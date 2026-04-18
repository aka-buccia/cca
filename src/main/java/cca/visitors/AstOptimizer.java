package cca.visitors;

import cca.FaaSChalCoreBaseVisitor;
import cca.FaaSChalCoreParser;
import cca.Node;
import cca.Position;
import cca.Program;
import cca.procedure.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

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

    }

    // Method for extracting token position
    private Position getPosition(Token t) {
        return new Position(this.file, t.getLine(), t.getCharPositionInLine());
    }

    private Position getPosition(ParserRuleContext c) {
        return getPosition(c.getStart());
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
