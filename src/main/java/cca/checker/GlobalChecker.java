package cca.checker;

import cca.exceptions.*;
import cca.ast.procedure.Procedure;
import cca.ast.Program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GlobalChecker {

    private static final String ENTRY_POINT_PROCEDURE_NAME = "main";

    public CompoundException check(Program program) {

        List<IllFormedException> errors = new ArrayList<>();

        Map<String, ProcedureInfo> procedureTable = new HashMap<>();

        for (Procedure p : program.procedures()) {
            String name = p.name().id();

            // check procedure name duplicates
            if (procedureTable.containsKey(name)) {
                errors.add(new IllFormedException(
                        p.position(),
                        "Procedure " + name + " already defined"));
                continue;
            }

            ProcedureSignature signature = new ProcedureSignature(
                    p.parameterList(),
                    ComputedTerminationOrder.compute(
                            p.parameterList(),
                            p.terminationOrder()));

            ProcedureInfo info = new ProcedureInfo(signature, p.choreography());
            procedureTable.put(name, info);
        }

        // If entry point procedure is missing, terminate
        if (!procedureTable.containsKey(ENTRY_POINT_PROCEDURE_NAME)) {
            errors.add(new IllFormedException(
                    program.position(),
                    "Procedure \"" + ENTRY_POINT_PROCEDURE_NAME + "\" is not defined"));
            return new CompoundException(errors);
        }

        Set<String> visited = new HashSet<>();
        Queue<String> reachableProcedures = new LinkedList<>();

        reachableProcedures.add(ENTRY_POINT_PROCEDURE_NAME);
        visited.add("main");

        LocalChecker localChecker = new LocalChecker();

        while (!reachableProcedures.isEmpty()) {
            String currentProcName = reachableProcedures.poll();
            ProcedureInfo currentProc = procedureTable.get(currentProcName);

            if (currentProc == null) {
                continue;
            }

            LocalCheckResult result = localChecker.check(procedureTable, currentProc);
            errors.addAll(result.getErrors());

            // reachability
            for (String calledProc : result.getDiscoveredCalls()) {
                if (!procedureTable.containsKey(calledProc)) {
                    continue;
                }
                if (visited.add(calledProc)) {
                    reachableProcedures.add(calledProc);
                }
            }
        }

        return new CompoundException(errors);
    }
}
