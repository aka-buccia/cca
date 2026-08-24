package cca.checker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import cca.ast.Node;
import cca.ast.Position;
import cca.ast.Role;
import cca.ast.choreography.*;
import cca.ast.instruction.*;
import cca.ast.procedure.*;
import cca.ast.visitors.AbstractVisitor;
import cca.exceptions.IllFormedException;

public class LocalChecker extends AbstractVisitor<Void> {

    private Map<String, ProcedureInfo> procedureMap;
    private Procedure procedure;
    private CheckerContext context;
    private List<IllFormedException> errors;

    public LocalChecker() {
    }

    public LocalChecker(Map<String, ProcedureInfo> procedureMap, Procedure procedure, CheckerContext context,
            List<IllFormedException> errors) {
        this.procedureMap = procedureMap;
        this.procedure = procedure;
        this.context = context;
        this.errors = errors;
    }

    public List<IllFormedException> check(Map<String, ProcedureInfo> procedureMap, Procedure procedure) {

        this.procedureMap = procedureMap;
        this.procedure = procedure;
        this.context = new CheckerContext();

        this.context.init(procedure);
        this.errors = new ArrayList<>();
        visit(procedure.choreography());

        return errors;
    }

    public List<IllFormedException> checkBranch(Map<String, ProcedureInfo> procedureMap, Choreography choreography,
            CheckerContext context) {

        this.procedureMap = procedureMap;
        this.procedure = null;
        this.context = context;

        this.errors = new ArrayList<>();
        visit(choreography);

        return errors;
    }

    @Override
    public Void visit(Choreography n) {

        for (Instruction i : n.instructions()) {
            visit(i);
        }
        visit(n.termination());

        return null;
    }

    @Override
    public Void visit(Terminated n) {

        List<TerminatingPair> terminatingPairs = context.getTerminatingPairs();

        // All terminating roles has to terminate before procedure termination
        // TODO: iterate on every role which hasn't been terminated
        if (!terminatingPairs.isEmpty()) {
            addError(n);
        }

        return null;
    }

    @Override
    public Void visit(Instruction n) {

        n.accept(this);
        return null;
    }

    @Override
    public Void visit(Communication n) {

        // Stateful roles has to be different
        if (n.leftRole().equals(n.rightRole())) {
            addError(n);
        }

        // Left role has to be stateful
        if (!context.isStateful(n.leftRole())) {
            addError(n.leftRole());
        }

        // Right role has to be stateful
        if (!context.isStateful(n.rightRole())) {
            addError(n.rightRole());
        }

        return null;
    }

    @Override
    public Void visit(Selection n) {

        // Stateful roles has to be different
        if (n.sourceRole().equals(n.targetRole())) {
            addError(n);
        }

        // Source role has to be stateful
        if (!context.isStateful(n.sourceRole())) {
            addError(n.sourceRole());
        }

        // Target role has to be stateful
        if (!context.isStateful(n.targetRole())) {
            addError(n.targetRole());
        }

        return null;
    }

    @Override
    public Void visit(Assignment n) {

        // Target role has to be defined as stateful or nonterminating
        if (!isDefined(n.targetRole())) {
            addError(n.targetRole());
        }

        return null;
    }

    @Override
    public Void visit(Request n) {

        // Source role has to be defined as stateful or nonterminating
        if (!isDefined(n.sourceRole())) {
            addError(n.sourceRole());
        }

        // Target role has to not be in the current scope
        if (n.targetRole() != null && !context.isStateful(n.targetRole())) {
            addError(n.targetRole());
        }

        addTerminatingPair(n.targetRole(), null);

        // Add target role to current scope
        context.addStateless(n.targetRole());

        return null;
    }

    @Override
    public Void visit(RequestResponse n) {

        // Source role has to be defined as stateful or nonterminating
        if (!isDefined(n.sourceRole())) {
            addError(n.sourceRole());
        }

        // Target role has to not be in the current scope
        if (n.targetRole() != null && !context.isStateful(n.targetRole())) {
            addError(n.targetRole());
        }

        addTerminatingPair(n.targetRole(), n.sourceRole());

        addOrderingCouple(n.targetRole(), n.sourceRole());
        computeTerminationOrderTransitiveClosure();

        // Add target role to current scope
        context.addStateless(n.targetRole());

        return null;
    }

    @Override
    public Void visit(End n) {

        // Ending role has to be a stateless role without creator
        if (isTerm(new TerminatingPair(n.endingRole(), null))) {
            addError(n.endingRole());
        }

        // Ending role has to be free from waiting a response
        if (!isFree(n.endingRole())) {
            addError(n.endingRole());
        }

        removeTerminatingPair(n.endingRole(), null);

        return null;
    }

    @Override
    public Void visit(EndResponse n) {

        // Ending role has to be a stateless role without creator
        if (isTerm(new TerminatingPair(n.endingRole(), null))) {
            addError(n.endingRole());
        }

        // Ending role has to be free from waiting a response
        if (!isFree(n.endingRole())) {
            addError(n.endingRole());
        }

        removeTerminatingPair(n.endingRole(), n.targetRole());
        removeOrderingCouplesWithLeft(n.endingRole());

        return null;
    }

    @Override
    public Void visit(Conditional n) {

        // Guard role has to be defined
        if (!context.isDefined(n.targetRole())) {
            addError(n.targetRole());
        }

        TerminatingRolesCollector collector = new TerminatingRolesCollector();

        Set<TerminatingPair> ifTerminated = collector.visit(n.ifBranch());
        Set<TerminatingPair> elseTerminated = collector.visit(n.elseBranch());

        // Define D, the set of terminating pairs that haven't terminated inside the two
        // branch
        List<TerminatingPair> D = new ArrayList<>(context.getTerminatingPairs());
        D.removeIf(tp -> ifTerminated.contains(tp) || elseTerminated.contains(tp));

        // Add an error for each term that ends in one and does not end in the other
        for (TerminatingPair tp : ifTerminated) {
            if (!elseTerminated.contains(tp)) {
                addError(n);
            }
        }
        for (TerminatingPair tp : elseTerminated) {
            if (!ifTerminated.contains(tp)) {
                addError(n);
            }
        }

        CheckerContext branchContext = this.context.copy();

        // Add D to nonterminating roles
        Set<Role> branchNonTerminating = new HashSet<>(context.getNonTerminatingRoles());
        for (TerminatingPair tp : D) {
            branchNonTerminating.add(tp.createdRole());
        }
        branchContext.setNonTerminatingRoles(branchNonTerminating);

        // Remove D from terminating roles
        List<TerminatingPair> branchTerminating = new ArrayList<>(context.getTerminatingPairs());
        branchTerminating.removeAll(D);
        branchContext.setTerminatingPairs(branchTerminating);

        // Remove ordering couples with left role in D
        for (TerminatingPair tp : D) {
            branchContext.removeOrderingCouplesWithLeft(tp.createdRole());
        }

        LocalChecker checker = new LocalChecker();

        // Visit if branch
        CheckerContext ifContext = branchContext.copy();
        List<IllFormedException> ifErrors = checker.checkBranch(procedureMap, n.ifBranch(), ifContext);

        // Visit else branch
        CheckerContext elseContext = branchContext.copy();
        List<IllFormedException> elseErrors = checker.checkBranch(procedureMap, n.elseBranch(), elseContext);

        // Insert errors
        this.errors.addAll(ifErrors);
        this.errors.addAll(elseErrors);

        // Set context for continuation

        // Remove ordering couples with term\D roles
        for (TerminatingPair tp : branchTerminating) {
            this.context.removeOrderingCouplesWithLeft(tp.createdRole());
        }

        // Set D as terminatingPairs
        this.context.setTerminatingPairs(D);

        // Add stateless created inside branchs
        Set<Role> continuationStateless = this.context.getStatelessRoles();
        continuationStateless.addAll(ifContext.getStatelessRoles());
        continuationStateless.addAll(elseContext.getStatelessRoles());
        this.context.setStatelessRoles(continuationStateless);

        return null;
    }

    @Override
    public Void visit(ProcedureCall n) {

        List<Role> actualStatefulRoles = n.parameterList().statefulParameters().stream()
                .map(StatefulParameter::parameter)
                .collect(Collectors.toList()); // iterate with p

        List<Role> actualNonTerminatingRoles = n.parameterList().nonTerminatingParameters().stream()
                .map(NonTerminatingParameter::parameter)
                .collect(Collectors.toList()); // iterate with n

        List<TerminatingPair> actualTerminatingPairs = n.parameterList().terminatingParameters().stream()
                .map(tp -> new TerminatingPair(tp.createdRole(), tp.creatorRole(), tp.position()))
                .collect(Collectors.toList()); // iterate with (f, s)

        // Actual stateful parameters has to be mentionable
        for (Role r : actualStatefulRoles) {
            if (!context.isStateful(r)) {
                addError(r);
            }
        }
        // ---------------------

        // Actual non terminating parameters has to be mentionable
        for (Role r : actualNonTerminatingRoles) {
            if (!context.isStateful(r) && !context.isNonTerm(r) && !context.isTerm(new TerminatingPair(r, null))) {
                addError(r);
            }
        }
        // ---------------------

        // Actual terminating parameters has to be mentionable
        for (TerminatingPair tp : actualTerminatingPairs) {
            if (!context.isTerm(tp)) {
                addError(tp.position());
            }
        }
        // ---------------------

        // actualStatefulRoles can't be duplicated
        // p_i != p_j
        Set<Role> statefulSet = new HashSet<>();
        for (Role r : actualStatefulRoles) {
            if (!statefulSet.add(r)) {
                addError(r);
            }
        }
        // ---------------------

        // actualNonTerminatingRoles can't be duplicated
        // n_i != n_j
        Set<Role> nonTermSet = new HashSet<>();
        for (Role r : actualNonTerminatingRoles) {
            if (!nonTermSet.add(r)) {
                addError(r);
            }
        }
        // ---------------------

        // left roles in actualTerminatingPairs can't be duplicated
        // f_i != f_j
        Set<Role> termLeftSet = new HashSet<>();
        for (TerminatingPair tp : actualTerminatingPairs) {
            if (!termLeftSet.add(tp.createdRole())) {
                addError(tp.position());
            }
        }
        // ---------------------

        // check p_i != n_j != f_k != s_k:

        // p_i != n_j
        for (Role r : statefulSet) {
            if (nonTermSet.contains(r)) {
                addError(r);
            }
        }

        // p_i and n_j != f_k
        for (Role r : termLeftSet) {
            if (statefulSet.contains(r) || nonTermSet.contains(r)) {
                addError(r);
            }
        }

        // For every actualTerminatingPair k: f_k != s_k
        // Checks also that every p_i, n_j != s_k
        for (TerminatingPair tp : actualTerminatingPairs) {
            Role created = tp.createdRole(); // f_k
            Role creator = tp.creatorRole(); // s_k

            if (creator != null) {
                // f_k != s_k
                if (created.equals(creator)) {
                    addError(tp.position());
                }
                // p_i != s_k and n_j != s_k
                if (statefulSet.contains(creator) || nonTermSet.contains(creator)) {
                    addError(tp.position());
                }
            }
        }
        // ---------------------

        // The procedure and his termination order must be in procedureMap
        if (!procedureMap.containsKey(n.name().id())) {
            addError(n.name());
            return null; // can't procede if procedure ins't defined
            // TODO implement continuation
        }
        // ---------------------

        ProcedureParameterList procedureCalledParameters = procedureMap.get(n.name().id()).signature().parameterList();
        List<Role> formalStatefulRoles = procedureCalledParameters.statefulParameters().stream()
                .map(StatefulParameter::parameter)
                .collect(Collectors.toList());

        List<Role> formalNonTerminatingRoles = procedureCalledParameters.nonTerminatingParameters().stream()
                .map(NonTerminatingParameter::parameter)
                .collect(Collectors.toList());

        List<TerminatingPair> formalTerminatingPairs = procedureCalledParameters.terminatingParameters().stream()
                .map(tp -> new TerminatingPair(tp.createdRole(), tp.creatorRole(), tp.position()))
                .collect(Collectors.toList());

        // n. of actual and formal stateful params has to be the same
        if (actualStatefulRoles.size() != formalStatefulRoles.size()) {
            addError(n.parameterList());
        }
        // ---------------------

        // n. of actual and formal non term params has to be the same
        if (actualNonTerminatingRoles.size() != formalNonTerminatingRoles.size()) {
            addError(n.parameterList());
        }
        // ---------------------

        // n. of actual and formal term params has to be the same
        if (actualTerminatingPairs.size() != formalTerminatingPairs.size()) {
            addError(n.parameterList());
        }
        // ---------------------

        // All formal term params has to match the corresponding formal param
        for (int index = 0; index < actualTerminatingPairs.size(); index++) {
            TerminatingPair actualTp = actualTerminatingPairs.get(index);

            if (index < formalTerminatingPairs.size()) {
                TerminatingPair expectedTp = formalTerminatingPairs.get(index);

                boolean actualRightIsNull = actualTp.creatorRole() == null;
                boolean expectedRightIsNull = expectedTp.creatorRole() == null;

                // Error if actual is 0 and formal no, or vice-versa
                if (actualRightIsNull != expectedRightIsNull) {
                    addError(actualTp.position());
                }
            }
        }
        // ---------------------

        // When two formal params are the same, the corresponding actual parm must be
        // the same
        List<Role> actualCreatorRoles = actualTerminatingPairs.stream()
                .map(TerminatingPair::creatorRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Role> formalCreatorRoles = formalTerminatingPairs.stream()
                .map(TerminatingPair::creatorRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // p_i == s_j in formal parameters must hold p_i == s_j in actual parameters
        checkCorrespondence(
                formalStatefulRoles, formalCreatorRoles,
                actualStatefulRoles, actualCreatorRoles,
                false);

        // n_i == s_j in formal parameters must hold n_i == s_j in actual parameters
        checkCorrespondence(
                formalNonTerminatingRoles, formalCreatorRoles,
                actualNonTerminatingRoles, actualCreatorRoles,
                false);

        // s_i == s_j in formal parameters must hold s_i == s_j in actual parameters
        checkCorrespondence(
                formalCreatorRoles, formalCreatorRoles,
                actualCreatorRoles, actualCreatorRoles,
                true);

        // ---------------------

        // Check that procedure call doesn't broke termination order
        List<TerminatingPair> stillTerminatingPairs = context.getTerminatingPairs();
        stillTerminatingPairs.removeAll(actualTerminatingPairs);

        Set<Role> stillTermLeftRoles = stillTerminatingPairs.stream()
                .map(TerminatingPair::createdRole)
                .collect(Collectors.toSet());

        Set<Role> actualTermLeftRoles = actualTerminatingPairs.stream()
                .map(TerminatingPair::createdRole)
                .collect(Collectors.toSet());

        Set<OrderingCouple> contextTerminationOrder = context.getTerminationOrder();

        // For every couple f,g verify that f <: g doesn't exists
        for (Role f : stillTermLeftRoles) {
            for (Role g : actualTermLeftRoles) {
                boolean existsInOrder = contextTerminationOrder.contains(createOrderingCouple(f, g));

                if (existsInOrder) {
                    addError(g);
                }
            }
        }
        // ---------------------

        // if in actualTerminatingPairs there's (f_i, s_i) and (s_i, s_j), then ordering
        // couple (f_i, f_j) must be declared in procedure called
        List<OrderingCouple> procedureTerminationOrder = procedureMap.get(n.name().id()).signature().terminationOrder()
                .elements();
        for (int i = 0; i < actualTerminatingPairs.size(); i++) {
            TerminatingPair pairI = actualTerminatingPairs.get(i);
            Role s_i = pairI.creatorRole();

            for (int j = 0; j < actualTerminatingPairs.size(); j++) {
                TerminatingPair pairJ = actualTerminatingPairs.get(j);
                Role f_j = pairJ.createdRole();

                if (s_i != null && s_i.equals(f_j)) {
                    Role f_i_p = formalTerminatingPairs.get(i).createdRole();
                    Role f_j_p = formalTerminatingPairs.get(j).createdRole();
                    OrderingCouple expectedOrderingCouple = createOrderingCouple(f_i_p, f_j_p);

                    if (!procedureTerminationOrder.contains(expectedOrderingCouple)) {
                        addError(s_i);
                    }
                }
            }
        }
        // ---------------------

        // for every ordering couple (f_i, f_j) in context must exist a (f_i_p, f_j_p)
        // ordering couple declared in procedure called
        for (int i = 0; i < actualTerminatingPairs.size(); i++) {
            Role f_i = actualTerminatingPairs.get(i).createdRole();
            for (int j = 0; j < actualTerminatingPairs.size(); j++) {
                Role f_j = actualTerminatingPairs.get(j).createdRole();

                OrderingCouple contextOrderingCouple = createOrderingCouple(f_i, f_j);

                if (contextTerminationOrder.contains(contextOrderingCouple)) {
                    Role f_i_p = formalTerminatingPairs.get(i).createdRole();
                    Role f_j_p = formalTerminatingPairs.get(j).createdRole();
                    OrderingCouple expectedOrderingCouple = createOrderingCouple(f_i_p, f_j_p);

                    if (!procedureTerminationOrder.contains(expectedOrderingCouple)) {
                        addError(f_j);
                    }

                }
            }
        }
        // ---------------------

        context.setTerminatingPairs(stillTerminatingPairs);

        // Remove ordering couples with roles terminated inside procedure
        for (TerminatingPair tp : actualTerminatingPairs) {
            this.context.removeOrderingCouplesWithLeft(tp.createdRole());
        }

        return null;
    }

    // Helpers

    private void addTerminatingPair(Role created, Role creator) {
        context.addTerminatingPair(created, creator);
    }

    private void removeTerminatingPair(Role created, Role creator) {
        context.removeTerminatingPair(created, creator);
    }

    private boolean isTerm(TerminatingPair t) {
        return context.isTerm(t);
    }

    private OrderingCouple createOrderingCouple(Role left, Role right) {
        return new OrderingCouple(left, right, left.position());
    }

    private void addOrderingCouple(Role left, Role right) {
        context.addOrderingCouple(left, right);
    }

    private void removeOrderingCouplesWithLeft(Role r) {
        context.removeOrderingCouplesWithLeft(r);
    }

    private void computeTerminationOrderTransitiveClosure() {
        context.computeTerminationOrderTransitiveClosure();
    }

    private boolean isDefined(Role r) {
        return context.isDefined(r);
    }

    private boolean isFree(Role r) {
        return context.isFree(r);
    }

    private void addError(Node n) {
        addError(n.position(), "");
    }

    private void addError(Position p) {
        addError(p, "");
    }

    private void addError(Position p, String message) {
        errors.add(
                new IllFormedException(
                        p,
                        message));
    }

    /**
     * Verifies that the equality relationship between expected roles at positions i
     * and j also holds between the actual roles at the same positions.
     */
    private void checkCorrespondence(
            List<Role> expListA,
            List<Role> expListB,
            List<Role> actListA,
            List<Role> actListB,
            boolean isSameList) {

        for (int i = 0; i < expListA.size(); i++) {
            Role r_i_exp = expListA.get(i);
            if (r_i_exp == null)
                continue;

            // If comparing the same list against itself, start from i + 1
            int startJ = isSameList ? i + 1 : 0;

            for (int j = startJ; j < expListB.size(); j++) {
                Role r_j_exp = expListB.get(j);
                if (r_j_exp == null)
                    continue;

                if (r_i_exp.equals(r_j_exp)) {
                    if (i < actListA.size() && j < actListB.size()) {
                        Role r_i_act = actListA.get(i);
                        Role r_j_act = actListB.get(j);

                        if (!r_i_act.equals(r_j_act)) {
                            addError(r_i_act);
                        }
                    }
                }
            }
        }
    }

}
