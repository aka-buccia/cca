package cca.interaction;

import cca.Media;
import cca.Position;
import cca.Role;
import cca.expression.Variable;
import cca.visitors.VisitorInterface;
import cca.expression.Expression;

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
