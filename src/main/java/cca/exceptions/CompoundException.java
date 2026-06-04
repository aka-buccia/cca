package cca.exceptions;

import java.util.List;

public class CompoundException extends FaaSChalCoreException {

    private List<? extends FaaSChalCoreException> causes;

    public CompoundException(List<? extends FaaSChalCoreException> causes) {
        this.causes = causes;
    }

    public List<? extends FaaSChalCoreException> getCauses() {
        return this.causes;
    }
}
