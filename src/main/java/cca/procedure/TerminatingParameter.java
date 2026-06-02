package cca.procedure;

import cca.Position;
import cca.Role;
import cca.visitors.VisitorInterface;

public abstract class TerminatingParameter extends ProcedureParameter {

    private final Role createdRole;

    public TerminatingParameter(Role createdRole, Position position) {
        this.createdRole = createdRole;
        super(position);
    }

    public Role createdRole() {
        return this.createdRole;
    }

    public static class TerminatingParameterCouple extends TerminatingParameter {

        private final Role creatorRole;

        public TerminatingParameterCouple(Role createdRole, Role creatorRole, Position position) {
            this.creatorRole = creatorRole;
            super(createdRole, position);
        }

        public Role creatorRole() {
            return this.creatorRole;
        }

        @Override
        public <R> R accept(VisitorInterface<R> v) {
            return v.visit(this);
        }
    }

    public static class TerminatingParameterSingle extends TerminatingParameter {

        public TerminatingParameterSingle(Role createdRole, Position position) {
            super(createdRole, position);
        }

        @Override
        public <R> R accept(VisitorInterface<R> v) {
            return v.visit(this);
        }
    }

}
