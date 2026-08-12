package cca.ast;

import cca.ast.visitors.VisitorInterface;

public class Role extends Node {

    private final Name name;

    public Role(Name name, Position position) {
        this.name = name;
        super(position);
    }

    @Override
    public <R> R accept(VisitorInterface<R> v) {
        return v.visit(this);
    }

    public Name name() {
        return name;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof Role) {
            return this.name.equals(((Role) o).name());
        } else {
            return false;
        }
    }

}
