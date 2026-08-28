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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cca.ast.Position;
import cca.ast.Program;
import cca.ast.visitors.PrettyPrinterVisitor;
import cca.checker.GlobalChecker;
import cca.exceptions.AstPositionedException;
import cca.exceptions.CompoundException;
import cca.exceptions.FaaSChalCoreException;
import cca.parser.Parser;
import cca.utils.VerbosityLevel;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "faasch", description = "A toolkit for parsing, formatting and static analysis of FaaSChalCore choreographies", subcommands = {
        FaaSChalCore.PrettyPrinter.class, FaaSChalCore.Checker.class }, mixinStandardHelpOptions = true)
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

    @Command(name = "prettify", aliases = { "p" }, description = "Pretty-print source files")
    static class PrettyPrinter extends FaaSChalCoreCommand implements Callable<Integer> {

        @Mixin
        OutputOptions outputOptions;

        @Override
        public Integer call() {

            List<Path> sources = sourcePathOption.getPaths();
            PrettyPrinterVisitor printer = new PrettyPrinterVisitor();

            try {
                for (Path source : sources) {

                    // Parse
                    Program p = Parser.parseSourceFile(source.toFile());

                    // Pretty-print
                    String prettyCode = printer.visit(p);

                    if (outputOptions.isDryRun()) {
                        System.out.println(prettyCode);
                    } else {
                        Path outputPath = outputOptions.targetpath()
                                .orElse(source);
                        Files.write(outputPath, prettyCode.getBytes());
                        if (verbosityOptions.verbosity().compareTo(VerbosityLevel.INFO) >= 0) {
                            System.out.println("Written to: " + outputPath);
                        }
                    }
                }
            } catch (Exception e) {
                printNiceErrorMessage(e, verbosityOptions.verbosity());
                System.err.println("prettify failed");
                return 1;
            }

            return 0;
        }
    }

    @Command(name = "check", aliases = { "c", "analysis" }, description = "Check if source files are well-formed")
    static class Checker extends FaaSChalCoreCommand implements Callable<Integer> {

        @Override
        public Integer call() {
            List<Path> sources = sourcePathOption.getPaths();
            GlobalChecker globalChecker = new GlobalChecker();

            try {
                for (Path source : sources) {
                    // Parse AST
                    Program p = Parser.parseSourceFile(source.toFile());

                    // Check well-formedness
                    globalChecker.check(p);

                    if (verbosityOptions.verbosity().compareTo(VerbosityLevel.INFO) >= 0) {
                        System.out.println("Checking " + source + ": OK");
                    }
                }
            } catch (Exception e) {
                printNiceErrorMessage(e, verbosityOptions.verbosity());
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

    protected static void printNiceErrorMessage(
            Throwable e, VerbosityLevel verbosity) {

        if (e instanceof AstPositionedException se) {
            Position p = se.getPosition();
            System.err.print(String.format("Error at %s: %s.\n%s", p, se.getMessage(), formattedSnippet(p)));
            if (verbosity == VerbosityLevel.DEBUG) {
                e.printStackTrace();
            }
        } else if (e instanceof CompoundException) {
            for (FaaSChalCoreException f : ((CompoundException) e).getCauses()) {
                printNiceErrorMessage(f, verbosity);
            }
        } else {
            System.err.println("Error:" + e.getMessage());
            if (verbosity == VerbosityLevel.DEBUG) {
                e.printStackTrace();
            }
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
