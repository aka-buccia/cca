package cca.ast.instruction;

import cca.ast.Media;
import cca.ast.Position;
import cca.ast.Role;
import cca.ast.expression.Variable;
import cca.ast.visitors.VisitorInterface;
import cca.ast.expression.Expression;

// sourceExpression@sourceRole -media-> targetVariable@targetRole |> sourceVariable@sourceRole

public class RequestResponse extends Request {

    private final Variable sourceVariable;

    public RequestResponse(Expression sourceExpression, Role sourceRole, Media media, Variable targetVariable,
            Role targetRole, Variable sourceVariable, Position position) {
        this.sourceVariable = sourceVariable;
        super(sourceExpression, sourceRole, media, targetVariable, targetRole, position);
    }

    public Variable sourceVariable() {
        return this.sourceVariable;
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }

}
