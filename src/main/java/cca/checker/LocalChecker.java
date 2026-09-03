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
import cca.checker.model.LocalCheckResult;
import cca.checker.model.ProcedureInfo;
import cca.checker.model.ProcedureSignature;
import cca.checker.model.TerminatingPair;
import cca.checker.util.TerminatingRolesCollector;
import cca.checker.util.TerminationOrderUtils;
import cca.exceptions.IllFormedException;

public class LocalChecker extends AbstractVisitor<Void> {

    private Map<String, cca.checker.model.ProcedureInfo> procedureMap;
    private CheckerContext context;
    private List<IllFormedException> errors;
    private Set<String> procedureCalled;

    public LocalChecker() {
    };

    public LocalChecker(Map<String, cca.checker.model.ProcedureInfo> procedureMap, CheckerContext context) {
        this.procedureMap = procedureMap;
        this.context = context;
        this.errors = new ArrayList<>();
        this.procedureCalled = new HashSet<>();
    }

    public cca.checker.model.LocalCheckResult check(Map<String, cca.checker.model.ProcedureInfo> procedureMap, ProcedureInfo procedureInfo) {

        this.procedureMap = procedureMap;
        this.errors = new ArrayList<>();
        this.procedureCalled = new HashSet<>();

        preVisitCheck(procedureInfo.signature());
        if (this.errors.size() > 0)
            return new cca.checker.model.LocalCheckResult(errors, procedureCalled);

        this.context = new CheckerContext();
        this.context.init(procedureInfo.signature());
        visit(procedureInfo.body());

        postVisitCheck(procedureInfo.signature());

        return new cca.checker.model.LocalCheckResult(errors, procedureCalled);
    }

    public cca.checker.model.LocalCheckResult checkBranch(Choreography choreography,
                                                          CheckerContext context) {

        this.context = context;

        this.errors = new ArrayList<>();
        visit(choreography);

        return new cca.checker.model.LocalCheckResult(errors, procedureCalled);
    }

    public void preVisitCheck(cca.checker.model.ProcedureSignature signature) {
        ProcedureParameterList params = signature.parameterList();
        Set<OrderingCouple> terminationOrder = signature.terminationOrder().getOrderingCouples();

        List<Role> statefulRoles = extractStatefulRoles(params);
        List<Role> nonTerminatingRoles = extractNonTerminatingRoles(params);
        List<cca.checker.model.TerminatingPair> terminatingPairs = extractTerminatingPairs(params);

        // all parameters must be distinct (with some exceptions for terminating pairs)
        checkRoleDuplicates(statefulRoles, "Stateful role duplicated: ");
        checkRoleDuplicates(nonTerminatingRoles, "Non-terminating role duplicated: ");
        checkTerminatingDuplicates(terminatingPairs);
        checkCrossParameterDisjointness(statefulRoles, nonTerminatingRoles, terminatingPairs);

        if (tranformProcedureParameterInRoleSet(params).size() < 1) {
            addError(params, "Procedure must have at least one formal parameter. Use a function instead");
        }

        if (!TerminationOrderUtils.isStrictPartialOrderOnClosedSet(signature.terminationOrder())) {
            addError(signature.terminationOrder().position(), "Termination order must be a strict partial order");
        }

        // this.errors.addAll(
        // TerminationOrderUtils.validateTerminationOrderInvariants(
        // terminationOrder, statefulRoles, nonTerminatingRoles, terminatingPairs));

    }

    public void postVisitCheck(ProcedureSignature signature) {
        Set<Role> finalMentionedRoles = context.getMentionedRoles();

        // body of the procedure must contain at least two roles
        if (finalMentionedRoles.size() < 2) {
            addError(signature.parameterList(),
                    "Body of the procedure must contain at least two roles, otherwise it can be expressed using a local function");
        }

        Set<Role> formalRoles = tranformProcedureParameterInRoleSet(signature.parameterList());

        // Procedure body must mention all formal parameters
        if (!finalMentionedRoles.containsAll(formalRoles)) {
            addError(signature.parameterList(), "Procedure body must mention all formal parameters");
        }

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

        List<cca.checker.model.TerminatingPair> terminatingPairs = context.getTerminatingPairs();

        // All terminating roles has to terminate before procedure termination
        if (!terminatingPairs.isEmpty()) {
            for (cca.checker.model.TerminatingPair missingPair : terminatingPairs) {
                addError(missingPair.createdRole().position(),
                        "Terminating created role '" + missingPair.createdRole()
                                + "' must terminate before procedure termination");
            }
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
        context.markRoleAsMentioned(n.leftRole());
        context.markRoleAsMentioned(n.rightRole());

        // Stateful roles has to be different
        if (n.leftRole().equals(n.rightRole())) {
            addError(n, "Source and target roles must be different");
        }

        // Left and right roles has to be stateful
        checkIsStateful(n.leftRole(), "Source role '" + n.leftRole() + "' must be stateful");
        checkIsStateful(n.rightRole(), "Target role '" + n.rightRole() + "' must be stateful");

        return null;
    }

    @Override
    public Void visit(Selection n) {
        context.markRoleAsMentioned(n.targetRole());
        context.markRoleAsMentioned(n.sourceRole());

        // Stateful roles has to be different
        if (n.sourceRole().equals(n.targetRole())) {
            addError(n, "Source and target roles must be different");
        }

        // Source and target roles has to be stateful
        checkIsStateful(n.sourceRole(), "Source role '" + n.sourceRole() + "' must be stateful");
        checkIsStateful(n.targetRole(), "Target role '" + n.targetRole() + "' must be stateful");

        return null;
    }

    @Override
    public Void visit(Assignment n) {
        context.markRoleAsMentioned(n.targetRole());

        // Check role is defined (stateful or stateless)
        checkIsDefined(n.targetRole());

        return null;
    }

    @Override
    public Void visit(Request n) {
        context.markRoleAsMentioned(n.targetRole());
        context.markRoleAsMentioned(n.sourceRole());

        // Check source role is defined
        checkIsDefined(n.sourceRole());
        // Target role must not be in scope
        checkNotInScope(n.targetRole(), "Target role '" + n.targetRole() + "' must not be in current scope");

        addTerminatingPair(n.targetRole(), null);
        // Add target role to current scope
        context.addStateless(n.targetRole());

        return null;
    }

    @Override
    public Void visit(RequestResponse n) {
        context.markRoleAsMentioned(n.targetRole());
        context.markRoleAsMentioned(n.sourceRole());

        // Source role has to be defined as stateful or nonterminating
        checkIsDefined(n.sourceRole());

        // Target role has to not be in the current scope
        checkNotInScope(n.targetRole(), "Target role '" + n.targetRole() + "' must not be in current scope");

        addTerminatingPair(n.targetRole(), n.sourceRole());
        addOrderingCouple(n.targetRole(), n.sourceRole());
        context.computeTerminationOrderTransitiveClosure();

        // Add target role to current scope
        context.addStateless(n.targetRole());

        return null;
    }

    @Override
    public Void visit(End n) {
        context.markRoleAsMentioned(n.endingRole());

        // Ending role has to be a stateless role without creator
        checkIsTerminatingPairValid(
                new cca.checker.model.TerminatingPair(n.endingRole(), null),
                "Ending role '" + n.endingRole() + "' must be a stateless role without creator");

        // Ending role has to be free from waiting a response
        checkIsFree(n.endingRole(), "Ending role '" + n.endingRole() + "' must be free from waiting a response");

        removeTerminatingPair(n.endingRole(), null);

        return null;
    }

    @Override
    public Void visit(EndResponse n) {
        context.markRoleAsMentioned(n.endingRole());
        context.markRoleAsMentioned(n.targetRole());

        // Ending role has to be a stateless role with a creator
        checkIsTerminatingPairValid(
                new cca.checker.model.TerminatingPair(n.endingRole(), n.targetRole()),
                "Ending role '" + n.endingRole() + "' must be a stateless role created by '" + n.targetRole() + "'");

        // Ending role has to be free from waiting a response
        checkIsFree(n.endingRole(), "Ending role '" + n.endingRole() + "' must be free from waiting a response");

        removeTerminatingPair(n.endingRole(), n.targetRole());
        removeOrderingCouplesWithLeft(n.endingRole());

        return null;
    }

    @Override
    public Void visit(Conditional n) {
        context.markRoleAsMentioned(n.targetRole());

        // Guard role has to be defined
        checkIsDefined(n.targetRole());

        cca.checker.util.TerminatingRolesCollector collector = new TerminatingRolesCollector();
        Set<cca.checker.model.TerminatingPair> ifTerminated = collector.visit(n.ifBranch());
        Set<cca.checker.model.TerminatingPair> elseTerminated = collector.visit(n.elseBranch());

        // Check that branches terminate the same roles
        checkBranchTerminatesEqually(n, ifTerminated, elseTerminated);

        // Define D, the set of terminating pairs that haven't terminated in either
        // branch
        List<cca.checker.model.TerminatingPair> D = new ArrayList<>(context.getTerminatingPairs());
        D.removeIf(tp -> ifTerminated.contains(tp) || elseTerminated.contains(tp));
        CheckerContext branchContext = createBranchContext(D);

        // Visit if and else branches
        CheckerContext ifContext = branchContext.copy();
        CheckerContext elseContext = branchContext.copy();
        LocalChecker checker = new LocalChecker(procedureMap, branchContext);

        cca.checker.model.LocalCheckResult ifBranchResponse = checker.checkBranch(n.ifBranch(), ifContext);
        cca.checker.model.LocalCheckResult elseBranchResponse = checker.checkBranch(n.elseBranch(), elseContext);

        // Collect errors, procedures called and mentioned roles
        gatherBranchResult(ifBranchResponse, ifContext);
        gatherBranchResult(elseBranchResponse, elseContext);

        // Set context for continuation
        setConditionalContinuation(D, ifContext, elseContext);

        return null;
    }

    @Override
    public Void visit(ProcedureCall n) {

        Set<Role> procedureCallMentionedRoles = tranformProcedureParameterInRoleSet(n.parameterList());
        context.markRolesAsMentioned(procedureCallMentionedRoles);

        // iterate stateful roles with p
        List<Role> actualStatefulRoles = extractStatefulRoles(n.parameterList());
        // iterate non terminating roles with n
        List<Role> actualNonTerminatingRoles = extractNonTerminatingRoles(n.parameterList());
        // iterate terminating pairs with (f, s)
        List<cca.checker.model.TerminatingPair> actualTerminatingPairs = extractTerminatingPairs(n.parameterList());

        checkActualParameters(actualStatefulRoles, actualNonTerminatingRoles, actualTerminatingPairs);

        // The procedure and his termination order must be in procedureMap
        if (!procedureMap.containsKey(n.name().id())) {
            addError(n.name(), "Procedure '" + n.name().id() + "' is not defined");
            setProcedureCallContinuation(actualTerminatingPairs);
            return null; // can't procede if procedure ins't defined
        }
        procedureCalled.add(n.name().id());
        // ---------------------

        ProcedureParameterList procedureCalledParameters = procedureMap.get(n.name().id()).signature().parameterList();
        List<Role> formalStatefulRoles = extractStatefulRoles(procedureCalledParameters);
        List<Role> formalNonTerminatingRoles = extractNonTerminatingRoles(procedureCalledParameters);
        List<cca.checker.model.TerminatingPair> formalTerminatingPairs = extractTerminatingPairs(procedureCalledParameters);

        checkFormalActualParamMatch(n.parameterList(), actualStatefulRoles, formalStatefulRoles,
                actualNonTerminatingRoles, formalNonTerminatingRoles, actualTerminatingPairs, formalTerminatingPairs);

        checkTerminationOrderPreservation(n, actualTerminatingPairs, formalTerminatingPairs);

        // Set context for continuation
        setProcedureCallContinuation(actualTerminatingPairs);

        return null;
    }

    // Helpers

    private boolean checkIsStateful(Role role, String errorMessage) {
        if (!context.isStateful(role)) {
            addError(role, errorMessage);
            return false;
        }
        return true;
    }

    private boolean checkIsDefined(Role role, String errorMessage) {
        if (!context.isDefined(role)) {
            addError(role, errorMessage);
            return false;
        }
        return true;
    }

    private boolean checkIsDefined(Role role) {
        return checkIsDefined(role, "Role '" + role + "' must be defined");
    }

    private boolean checkNotInScope(Role role, String errorMessage) {
        if (context.isInScope(role)) {
            addError(role, errorMessage);
            return false;
        }
        return true;
    }

    private boolean checkIsTerminatingPairValid(cca.checker.model.TerminatingPair tp, String errorMessage) {
        if (!context.isTerm(tp)) {
            addError(tp.position(), errorMessage);
            return false;
        }
        return true;
    }

    private boolean checkIsFree(Role role, String errorMessage) {
        if (!context.isFree(role)) {
            addError(role, errorMessage);
            return false;
        }
        return true;
    }

    private void checkBranchTerminatesEqually(
            Conditional n,
            Set<cca.checker.model.TerminatingPair> ifTerminated,
            Set<cca.checker.model.TerminatingPair> elseTerminated) {

        for (cca.checker.model.TerminatingPair tp : ifTerminated) {
            if (!elseTerminated.contains(tp)) {
                addError(n, "Terminating role '" + tp.createdRole()
                        + "' terminates in 'if' branch but not in 'else' branch");
            }
        }
        for (cca.checker.model.TerminatingPair tp : elseTerminated) {
            if (!ifTerminated.contains(tp)) {
                addError(n, "Terminating role '" + tp.createdRole()
                        + "' terminates in 'else' branch but not in 'if' branch");
            }
        }
    }

    private CheckerContext createBranchContext(List<cca.checker.model.TerminatingPair> D) {
        CheckerContext branchContext = this.context.copy();

        // Add D to nonterminating roles
        Set<Role> branchNonTerminating = new HashSet<>(context.getNonTerminatingRoles());
        for (cca.checker.model.TerminatingPair tp : D) {
            branchNonTerminating.add(tp.createdRole());
        }
        branchContext.setNonTerminatingRoles(branchNonTerminating);

        // Remove D from terminating roles
        List<cca.checker.model.TerminatingPair> branchTerminating = new ArrayList<>(context.getTerminatingPairs());
        branchTerminating.removeAll(D);
        branchContext.setTerminatingPairs(branchTerminating);

        // Remove ordering couples with left role in D
        for (cca.checker.model.TerminatingPair tp : D) {
            branchContext.removeOrderingCouplesWithLeft(tp.createdRole());
        }

        return branchContext;
    }

    private void gatherBranchResult(LocalCheckResult branchResponse, CheckerContext branchContext) {
        // Insert errors
        this.errors.addAll(branchResponse.getErrors());

        // Insert procedure called
        this.procedureCalled.addAll(branchResponse.getDiscoveredCalls());

        // Insert mentioned roles
        context.markRolesAsMentioned(branchContext.getMentionedRoles());
    }

    private void setConditionalContinuation(List<cca.checker.model.TerminatingPair> D, CheckerContext ifContext,
                                            CheckerContext elseContext) {

        // Remove ordering couples with roles that terminated inside branches (term \ D)
        List<cca.checker.model.TerminatingPair> branchTerminating = new ArrayList<>(context.getTerminatingPairs());
        branchTerminating.removeAll(D);

        // Remove ordering couples with term\D roles
        for (cca.checker.model.TerminatingPair tp : branchTerminating) {
            this.context.removeOrderingCouplesWithLeft(tp.createdRole());
        }

        // Set D as terminatingPairs
        this.context.setTerminatingPairs(D);

        // Add stateless created inside branchs
        Set<Role> continuationStateless = this.context.getStatelessRoles();
        continuationStateless.addAll(ifContext.getStatelessRoles());
        continuationStateless.addAll(elseContext.getStatelessRoles());
        this.context.setStatelessRoles(continuationStateless);

    }

    private void checkActualParameters(
            List<Role> actualStatefulRoles,
            List<Role> actualNonTerminatingRoles,
            List<cca.checker.model.TerminatingPair> actualTerminatingPairs) {

        // Actual stateful parameters has to be mentionable
        for (Role r : actualStatefulRoles) {
            checkIsStateful(r, "Actual stateful parameter '" + r + "' must be stateful");
        }
        // ---------------------

        // Actual non terminating parameters has to be mentionable
        for (Role r : actualNonTerminatingRoles) {
            if (!context.isStateful(r) && !context.isNonTerm(r) && !context.isTerm(new cca.checker.model.TerminatingPair(r, null))) {
                addError(r, "Invalid actual non-terminating parameter '" + r + "'");
            }
        }
        // ---------------------

        // Actual terminating parameters has to be mentionable
        for (cca.checker.model.TerminatingPair tp : actualTerminatingPairs) {
            checkIsTerminatingPairValid(tp, "Invalid actual terminating parameter '" + tp + "'");
        }
        // ---------------------

        // actualStatefulRoles can't be duplicated
        // p_i != p_j
        checkRoleDuplicates(actualStatefulRoles, "Duplicate actual stateful parameter");

        // actualNonTerminatingRoles can't be duplicated
        // n_i != n_j
        checkRoleDuplicates(actualNonTerminatingRoles, "Duplicate actual non-terminating parameter");
        // ---------------------

        // left roles in actualTerminatingPairs can't be duplicated
        // f_i != f_j
        checkTerminatingDuplicates(actualTerminatingPairs);
        // ---------------------

        // check p_i != n_j != f_k != s_k:
        checkCrossParameterDisjointness(actualStatefulRoles, actualNonTerminatingRoles, actualTerminatingPairs); // ---------------------
        // ---------------------
    }

    private void checkFormalActualParamMatch(
            ProcedureParameterList actualParamList,
            List<Role> actualStatefulRoles, List<Role> formalStatefulRoles,
            List<Role> actualNonTerminatingRoles, List<Role> formalNonTerminatingRoles,
            List<cca.checker.model.TerminatingPair> actualTerminatingPairs, List<cca.checker.model.TerminatingPair> formalTerminatingPairs) {

        // n. of actual and formal stateful params has to be the same
        if (actualStatefulRoles.size() != formalStatefulRoles.size()) {
            addError(actualParamList, "Stateful parameters count mismatch");
        }
        // ---------------------

        // n. of actual and formal non term params has to be the same
        if (actualNonTerminatingRoles.size() != formalNonTerminatingRoles.size()) {
            addError(actualParamList, "Non-terminating parameters count mismatch");
        }
        // ---------------------

        // n. of actual and formal term params has to be the same
        if (actualTerminatingPairs.size() != formalTerminatingPairs.size()) {
            addError(actualParamList, "Terminating parameters count mismatch");
        }
        // ---------------------

        // All formal term params has to match the corresponding formal param
        for (int index = 0; index < actualTerminatingPairs.size(); index++) {
            cca.checker.model.TerminatingPair actualTp = actualTerminatingPairs.get(index);

            if (index < formalTerminatingPairs.size()) {
                cca.checker.model.TerminatingPair expectedTp = formalTerminatingPairs.get(index);

                boolean actualRightIsNull = actualTp.creatorRole() == null;
                boolean expectedRightIsNull = expectedTp.creatorRole() == null;

                // Error if actual is 0 and formal no, or vice-versa
                if (actualRightIsNull != expectedRightIsNull) {
                    addError(actualTp.position(), "Mismatch in creator role presence for terminating parameter");
                }
            }
        }
        // ---------------------

        // When two formal params are the same, the corresponding actual param must be
        // the same
        List<Role> actualCreatorRoles = actualTerminatingPairs.stream()
                .map(cca.checker.model.TerminatingPair::creatorRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Role> formalCreatorRoles = formalTerminatingPairs.stream()
                .map(cca.checker.model.TerminatingPair::creatorRole)
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

    }

    private void checkTerminationOrderPreservation(ProcedureCall n,
            List<cca.checker.model.TerminatingPair> actualTerminatingPairs,
            List<cca.checker.model.TerminatingPair> formalTerminatingPairs) {

        // Check that procedure call doesn't broke termination order
        List<cca.checker.model.TerminatingPair> stillTerminatingPairs = context.getTerminatingPairs();
        stillTerminatingPairs.removeAll(actualTerminatingPairs);

        Set<Role> stillTermLeftRoles = stillTerminatingPairs.stream()
                .map(cca.checker.model.TerminatingPair::createdRole)
                .collect(Collectors.toSet());

        Set<Role> actualTermLeftRoles = actualTerminatingPairs.stream()
                .map(cca.checker.model.TerminatingPair::createdRole)
                .collect(Collectors.toSet());

        Set<OrderingCouple> contextTerminationOrder = context.getTerminationOrder();

        // For every couple f,g verify that f <: g doesn't exists
        for (Role f : stillTermLeftRoles) {
            for (Role g : actualTermLeftRoles) {
                boolean existsInOrder = contextTerminationOrder.contains(createOrderingCouple(f, g));

                if (existsInOrder) {
                    addError(g, "Procedure call breaks termination order for role '" + g
                            + "'. It should terminate after '" + f + "'");
                }
            }
        }
        // ---------------------

        // if in actualTerminatingPairs there's (f_i, s_i) and (s_i, s_j), then ordering
        // couple (f_i, f_j) must be declared in procedure called
        Set<OrderingCouple> procedureTerminationOrder = procedureMap.get(n.name().id())
                .signature().terminationOrder().getOrderingCouples();
        for (int i = 0; i < actualTerminatingPairs.size(); i++) {
            cca.checker.model.TerminatingPair pairI = actualTerminatingPairs.get(i);
            Role s_i = pairI.creatorRole();

            for (int j = 0; j < actualTerminatingPairs.size(); j++) {
                cca.checker.model.TerminatingPair pairJ = actualTerminatingPairs.get(j);
                Role f_j = pairJ.createdRole();

                if (s_i != null && s_i.equals(f_j)) {
                    Role f_i_p = formalTerminatingPairs.get(i).createdRole();
                    Role f_j_p = formalTerminatingPairs.get(j).createdRole();
                    OrderingCouple expectedOrderingCouple = createOrderingCouple(f_i_p, f_j_p);

                    if (!procedureTerminationOrder.contains(expectedOrderingCouple)) {
                        addError(s_i,
                                "Missing required ordering couple in called procedure for stateless creator '" + s_i
                                        + "'");
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
                        addError(f_j,
                                "Termination order in context not declared in procedure call for role '" + f_j + "'");
                    }

                }
            }
        }
        // ---------------------
    }

    private void setProcedureCallContinuation(List<cca.checker.model.TerminatingPair> actualTerminatingPairs) {
        List<cca.checker.model.TerminatingPair> stillTerminatingPairs = context.getTerminatingPairs();
        stillTerminatingPairs.removeAll(actualTerminatingPairs);

        context.setTerminatingPairs(stillTerminatingPairs);

        // Remove ordering couples with roles terminated inside procedure
        for (cca.checker.model.TerminatingPair tp : actualTerminatingPairs) {
            this.context.removeOrderingCouplesWithLeft(tp.createdRole());
        }
    }

    private List<Role> extractStatefulRoles(ProcedureParameterList list) {
        return list.statefulParameters().stream()
                .map(StatefulParameter::parameter)
                .collect(Collectors.toList());
    }

    private List<Role> extractNonTerminatingRoles(ProcedureParameterList list) {
        return list.nonTerminatingParameters().stream()
                .map(NonTerminatingParameter::parameter)
                .collect(Collectors.toList());
    }

    private List<cca.checker.model.TerminatingPair> extractTerminatingPairs(ProcedureParameterList list) {
        return list.terminatingParameters().stream()
                .map(tp -> new cca.checker.model.TerminatingPair(tp.createdRole(), tp.creatorRole(), tp.position()))
                .collect(Collectors.toList());
    }

    private void addTerminatingPair(Role created, Role creator) {
        context.addTerminatingPair(created, creator);
    }

    private void removeTerminatingPair(Role created, Role creator) {
        context.removeTerminatingPair(created, creator);
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

    private void addError(Node n, String message) {
        addError(n.position(), message);
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
                            addError(r_i_act,
                                    "Actual role '" + r_i_act + "' must correspond to actual role '" + r_j_act + "'");
                        }
                    }
                }
            }
        }
    }

    private Set<Role> tranformProcedureParameterInRoleSet(ProcedureParameterList parameterList) {
        Set<Role> result = new HashSet<>();

        result.addAll(parameterList.statefulParameters().stream()
                .map(StatefulParameter::parameter)
                .collect(Collectors.toSet()));
        result.addAll(parameterList.nonTerminatingParameters().stream()
                .map(NonTerminatingParameter::parameter)
                .collect(Collectors.toSet()));
        result.addAll(parameterList.terminatingParameters().stream()
                .map(TerminatingParameter::createdRole)
                .collect(Collectors.toSet()));
        result.addAll(parameterList.terminatingParameters().stream()
                .map(TerminatingParameter::creatorRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        return result;

    }

    private boolean checkRoleDuplicates(List<Role> roles, String errorMessagePrefix) {
        Set<Role> seen = new HashSet<>();
        boolean hasDuplicates = false;

        for (Role r : roles) {
            if (r != null && !seen.add(r)) {
                addError(r, errorMessagePrefix + " '" + r + "'");
                hasDuplicates = true;
            }
        }
        return hasDuplicates;
    }

    /**
     * Check if there are duplicate roles on the left side (createdRole) of the
     * TerminatingPair.
     */
    private boolean checkTerminatingDuplicates(List<cca.checker.model.TerminatingPair> pairs) {
        Set<Role> seenLeft = new HashSet<>();
        boolean hasDuplicates = false;

        for (cca.checker.model.TerminatingPair tp : pairs) {
            Role created = tp.createdRole();
            if (created != null && !seenLeft.add(created)) {
                addError(tp.position(), "Terminating created role '" + created + "' duplicated");
                hasDuplicates = true;
            }
        }
        return hasDuplicates;
    }

    /**
     * Check the disjunction between the various types of parameters (p_i != n_j !=
     * f_k != s_k).
     */
    private void checkCrossParameterDisjointness(
            List<Role> statefulRoles,
            List<Role> nonTerminatingRoles,
            List<cca.checker.model.TerminatingPair> terminatingPairs) {

        Set<Role> statefulSet = new HashSet<>(statefulRoles);
        Set<Role> nonTermSet = new HashSet<>(nonTerminatingRoles);

        // p_i != n_j
        for (Role r : statefulRoles) {
            if (nonTermSet.contains(r)) {
                addError(r, "Role '" + r + "' cannot be both stateful and non-terminating");
            }
        }

        // p_i and n_j != f_k
        for (TerminatingPair tp : terminatingPairs) {
            Role created = tp.createdRole();
            if (statefulSet.contains(created)) {
                addError(tp.position(), "Created role '" + created + "' cannot be a stateful parameter");
            }
            if (nonTermSet.contains(created)) {
                addError(tp.position(), "Created role '" + created + "' cannot be a non-terminating parameter");
            }

            Role creator = tp.creatorRole();
            if (creator != null) {
                // f_k != s_k
                if (created.equals(creator)) {
                    addError(tp.position(), "Created role and creator role cannot be the same: " + created);
                }
            }
        }
    }

}
