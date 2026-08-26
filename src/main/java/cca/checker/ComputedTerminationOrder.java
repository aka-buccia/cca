package cca.checker;

import cca.ast.procedure.ProcedureParameterList;
import cca.ast.procedure.TerminationOrder;
import cca.ast.procedure.OrderingCouple;
import cca.ast.Position;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class ComputedTerminationOrder {

    private final Set<OrderingCouple> orderingCouples;
    private final Position position;

    private ComputedTerminationOrder(Set<OrderingCouple> orderingCouples, Position position) {
        this.orderingCouples = new HashSet<>(orderingCouples);
        this.position = position;
    }

    public Set<OrderingCouple> getOrderingCouples() {
        return Collections.unmodifiableSet(this.orderingCouples);
    }

    public Position position() {
        return this.position;
    }

    public static ComputedTerminationOrder compute(
            ProcedureParameterList params,
            TerminationOrder declaredOrder) {

        Objects.requireNonNull(params, "params cannot be null");
        Objects.requireNonNull(declaredOrder, "declaredOrder cannot be null");

        Set<OrderingCouple> baseCouples = new HashSet<>();
        if (declaredOrder.elements() != null) {
            baseCouples.addAll(declaredOrder.elements());
        }

        // add terminatingPairs as OrderingCouples
        if (params.terminatingParameters() != null) {
            params.terminatingParameters().stream()
                    .filter(tp -> tp.creatorRole() != null)
                    .map(tp -> new OrderingCouple(tp.createdRole(), tp.creatorRole(), tp.position()))
                    .forEach(baseCouples::add);
        }

        // compute transitive closure
        Set<OrderingCouple> closureCouples = TerminationOrderUtils.computeTransitiveClosure(baseCouples);

        return new ComputedTerminationOrder(closureCouples, declaredOrder.position());
    }
}
