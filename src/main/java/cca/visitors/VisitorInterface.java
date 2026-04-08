package cca.visitors;

import cca.Program;

public interface VisitorInterface<R> {

    R visit(Program n);

}
