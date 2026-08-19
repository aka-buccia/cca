package cca.checker;

import java.util.Objects;

import cca.ast.Position;
import cca.ast.Role;

public class TerminatingPair {

    private final Role createdRole;
    private final Role creatorRole;
    private final Position position;

    public TerminatingPair(Role createdRole, Role creatorRole, Position position) {
        this.createdRole = createdRole;
        this.creatorRole = creatorRole;
        this.position = position;
    }

    public Role createdRole() {
        return this.createdRole;
    }

    public Role creatorRole() {
        return this.creatorRole;
    }

    public Position position() {
        return this.position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdRole, creatorRole);
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof TerminatingPair) {
            return this.createdRole.equals(((TerminatingPair) o).createdRole())
                    & this.creatorRole.equals(((TerminatingPair) o).creatorRole());
        } else {
            return false;
        }
    }

}
