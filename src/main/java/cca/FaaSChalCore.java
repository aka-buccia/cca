package cca;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import cca.ast.Position;
import cca.ast.Program;
import cca.ast.visitors.PrettyPrinterVisitor;
import cca.checker.GlobalChecker;
import cca.exceptions.*;
import cca.parser.Parser;
import cca.utils.VerbosityLevel;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "faasch", description = "A toolkit for parsing, formatting and static analysis of FaaSChalCore choreographies", subcommands = {
        FaaSChalCore.PrettyPrinter.class,
        FaaSChalCore.Checker.class }, mixinStandardHelpOptions = true, version = "1.0.0")
public class FaaSChalCore extends FaaSChalCoreCommand implements Callable<Integer> {

    public static void main(String[] args) {
        System.exit(compile(args));
    }

    public static int compile(String[] args) {
        CommandLine cl = new CommandLine(new FaaSChalCore());
        cl.setUnmatchedOptionsArePositionalParams(true);
        return cl.execute(args);
    }

    @Override
    public Integer call() throws Exception {
        new CommandLine(this).usage(System.err);
        return 1;
    }

    @Command(name = "prettify", aliases = {
            "p" }, description = "Pretty-print source files", mixinStandardHelpOptions = true)
    static class PrettyPrinter extends FaaSChalCoreCommand implements Callable<Integer> {

        @Mixin
        OutputOptions outputOptions;

        @Override
        public Integer call() {
            List<Path> sources = sourcePathOption.getPaths();
            boolean globalSuccess = true;

            for (Path source : sources) {
                boolean success = parseAndProcess(source, p -> {
                    PrettyPrinterVisitor pp = new PrettyPrinterVisitor();
                    System.out.println(pp.visit(p));
                });

                if (!success)
                    globalSuccess = false;
            }

            if (!globalSuccess) {
                System.err.println("prettify failed");
                return 1;
            }
            return 0;
        }
    }

    @Command(name = "check", aliases = { "c",
            "analysis" }, description = "Check if source files are well-formed", mixinStandardHelpOptions = true)
    static class Checker extends FaaSChalCoreCommand implements Callable<Integer> {

        @Override
        public Integer call() {
            List<Path> sources = sourcePathOption.getPaths();
            GlobalChecker globalChecker = new GlobalChecker();
            boolean globalSuccess = true;

            for (Path source : sources) {
                boolean success = parseAndProcess(source, p -> {
                    try {
                        globalChecker.check(p);
                        if (verbosityOptions.verbosity().compareTo(VerbosityLevel.INFO) >= 0) {
                            System.out.println("Checking " + source + ": WELL-FORMED");
                        }
                    } catch (CompoundException e) {
                        // split errors and warnings
                        boolean hasErrors = e.getCauses().stream()
                                .anyMatch(cause -> !(cause instanceof WarningException));

                        printNiceErrorMessage(e, verbosityOptions.verbosity());

                        if (hasErrors) {
                            System.err.println("Checking " + source + ": ILL-FORMED");
                        } else {
                            if (verbosityOptions.verbosity().compareTo(VerbosityLevel.INFO) >= 0) {
                                System.out.println("Checking " + source + ": WELL-FORMED (with warnings)");
                            }
                        }
                    }
                });

                if (!success) {
                    globalSuccess = false;
                }
            }

            if (!globalSuccess) {
                System.err.println("check failed");
                return 1;
            }
            return 0;
        }
    }

}

class VerbosityOptions {

    private VerbosityLevel verbosity = VerbosityLevel.INFO;

    public VerbosityLevel verbosity() {
        return this.verbosity;
    }

    @Option(names = {
            "--verbosity" }, description = "Verbosity level: ${COMPLETION-CANDIDATES}", paramLabel = "<LEVEL>")
    private void setVerbosity(VerbosityLevel value) {
        this.verbosity = value;
    }

    @Option(names = { "-q",
            "--quiet" }, description = "Disable all messages except errors")
    private void setQuietLevel(boolean value) {
        if (value) {
            this.setVerbosity(VerbosityLevel.ERRORS);
        }
    }

    @Option(names = { "--debug", "--verbose",
            "-d" }, description = "Enable debug messages")
    private void setDebugLevel(boolean value) {
        if (value) {
            this.setVerbosity(VerbosityLevel.DEBUG);
        }
    }
}

@Command()
abstract class PathOption {
    private String value;

    private List<Path> paths;

    protected void setValue(String value) {
        this.value = value;
    }

    public final String value() {
        return value;
    }

    public final List<Path> getPaths() {
        return getPaths(false);
    }

    public final List<Path> getPaths(boolean cwdIfEmpty) {
        if (paths == null) {
            paths = new LinkedList<>();
            if (value != null) {
                for (String p : value().split(File.pathSeparator)) {
                    paths.add(Paths.get(p));
                }
            }
        }
        if (cwdIfEmpty && paths.isEmpty()) {
            paths.add(Paths.get(""));
        }
        return paths;
    }

    public final static class SourcePathOption extends PathOption {
        @Option(names = { "-s",
                "--sources" }, paramLabel = "<PATH>", description = "Specify where to find .faasch source files")
        @Override
        protected void setValue(String value) {
            super.setValue(value);
        }
    }
}

@Command()
class OutputOptions {

    @Option(names = { "--dry-run" }, description = "Disable any write on disk")
    private boolean dryRun = false;

    @Option(names = { "-t",
            "--target" }, paramLabel = "<PATHS>", description = "Specify where to write prettified source code")
    private Path targetpath;

    public boolean isDryRun() {
        return dryRun;
    }

    public Optional<Path> targetpath() {
        return Optional.ofNullable(targetpath);
    }
}

abstract class FaaSChalCoreCommand {

    @Mixin
    VerbosityOptions verbosityOptions;

    @Mixin
    PathOption.SourcePathOption sourcePathOption;

    /**
     * @param source    file to parse
     * @param processor logic to execute after parsing
     * @return result of parsing
     */
    protected boolean parseAndProcess(Path source, Consumer<Program> processor) {
        try {
            Program p = Parser.parseSourceFile(source.toFile());
            processor.accept(p);
            return true;

        } catch (CompoundException e) {
            boolean hasSyntaxErrors = e.getCauses().stream()
                    .anyMatch(cause -> cause instanceof SyntaxException);

            printNiceErrorMessage(e, verbosityOptions.verbosity());
            if (hasSyntaxErrors)
                System.err.println("Parsing " + source + ": " + "SYNTAX ERROR");
            return false;

        } catch (Exception e) {
            printNiceErrorMessage(e, verbosityOptions.verbosity());
            return false;
        }
    }

    protected static void printNiceErrorMessage(
            Throwable e, VerbosityLevel verbosity) {

        if (e instanceof CompoundException compound) {
            for (FaaSChalCoreException f : compound.getCauses()) {
                printNiceErrorMessage(f, verbosity);
            }
            return;
        }

        boolean isWarning = e instanceof WarningException;
        String label = isWarning ? "Warning" : "Error";
        String message = e.getMessage();

        if (e instanceof AstPositioned positioned) {
            Position p = positioned.getPosition();
            System.err.print(String.format("%s at %s: %s.\n%s", label, p, message, formattedSnippet(p)));
        } else {
            System.err.println(label + ": " + message);
        }

        if (verbosity == VerbosityLevel.DEBUG) {
            e.printStackTrace();
        }
    }

    private static String formattedSnippet(Position p) {
        if (p == null || p.sourceFile() == null || p.sourceFile().isBlank()) {
            return "";
        }

        Path path = Paths.get(p.sourceFile());

        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            return "";
        }

        try {
            List<String> lines = Files.readAllLines(path);
            int lineNum = p.line();

            if (lineNum <= 0 || lineNum > lines.size()) {
                return "";
            }

            int startLine = Math.max(0, lineNum - 2);
            int endLine = Math.min(lines.size(), lineNum + 1);

            List<String> snippetLines = lines.subList(startLine, endLine);
            StringBuilder sb = new StringBuilder();

            int baseLineNum = startLine + 1;
            int maxDigits = String.valueOf(endLine).length();

            for (int i = 0; i < snippetLines.size(); i++) {
                int currentLineNum = baseLineNum + i;
                int digits = String.valueOf(currentLineNum).length();
                int leftPad = 2 + (maxDigits - digits);

                String linePrefix = " ".repeat(leftPad) + currentLineNum + " | ";
                sb.append(linePrefix)
                        .append(snippetLines.get(i))
                        .append('\n');

                if (currentLineNum == lineNum) {
                    int caretIndent = linePrefix.length() + Math.max(0, p.column() - 1);
                    sb.append(" ".repeat(caretIndent))
                            .append('^')
                            .append('\n');
                }
            }
            return sb.toString();
        } catch (IOException ex) {
            return "";
        }
    }
}
