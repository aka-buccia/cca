package cca.checker;

import cca.checker.model.ComputedTerminationOrder;
import cca.checker.model.LocalCheckResult;
import cca.checker.model.ProcedureInfo;
import cca.checker.model.ProcedureSignature;
import cca.exceptions.*;
import cca.ast.procedure.Procedure;
import cca.ast.Program;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalChecker {

    private static final String ENTRY_POINT_PROCEDURE_NAME = "main";

    public void check(Program program) {

        List<FaaSChalCoreException> errors = new CopyOnWriteArrayList<>();

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
            throw new CompoundException(errors);
        }

        Set<String> calledProcedures = ConcurrentHashMap.newKeySet();
        calledProcedures.add(ENTRY_POINT_PROCEDURE_NAME);

        // Check procedures in parallel
        procedureTable.entrySet().parallelStream().forEach(entry -> {
            ProcedureInfo procInfo = entry.getValue();

            LocalChecker localChecker = new LocalChecker();
            LocalCheckResult result = localChecker.check(procedureTable, procInfo);

            errors.addAll(result.getErrors());
            calledProcedures.addAll(result.getDiscoveredCalls());
        });

        // reachability warnings
        for (Procedure p : program.procedures()) {
            String procName = p.name().id();

            if (!calledProcedures.contains(procName)) {
                errors.add(new UncalledProcedureWarning(
                        p.name().position(),
                        procName));
            }
        }

        if (!errors.isEmpty()) {
            throw new CompoundException(errors);
        }
    }
}
