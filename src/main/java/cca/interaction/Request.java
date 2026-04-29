package cca.interaction;

import cca.expression.*;
import cca.Position;
import cca.visitors.VisitorInterface;
import cca.Role;
import cca.Media;

public class Request extends Interaction {

    private final Expression sourceExpression;
    private final Role sourceRole;
    private final Media media;
    private final Variable targetVariable;
    private final Role targetRole;

    public Request(Expression sourceExpression, Role sourceRole, Media media, Variable targetVariable,
            Role targetRole,
            Position position) {
        this.sourceExpression = sourceExpression;
        this.sourceRole = sourceRole;
        this.media = media;
        this.targetVariable = targetVariable;
        this.targetRole = targetRole;

        super(position);
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }

}
