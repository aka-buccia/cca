package cca.checker;

import cca.ast.Program;
import cca.exceptions.CompoundException;
import cca.exceptions.WarningException;
import cca.parser.Parser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckerTest {

    private static final Path VALID_RESOURCES_PATH = Paths.get("src/test/resources/checker/valid");
    private static final Path WARNING_RESOURCES_PATH = Paths.get("src/test/resources/checker/warning");
    private static final Path INVALID_RESOURCES_PATH = Paths.get("src/test/resources/checker/invalid");

    private final GlobalChecker globalChecker = new GlobalChecker();

    private static Stream<File> getFaaschFilesFrom(Path dirPath) throws IOException {
        if (!Files.exists(dirPath)) {
            return Stream.empty();
        }
        return Files.list(dirPath)
                .filter(path -> path.toString().endsWith(".faasch"))
                .map(Path::toFile);
    }

    // Provides all .faasch files from the valid test resources directory
    private static Stream<File> validTestFiles() throws IOException {
        return getFaaschFilesFrom(VALID_RESOURCES_PATH);
    }

    // Provides all .faasch files from the warning test resources directory
    private static Stream<File> warningTestFiles() throws IOException {
        return getFaaschFilesFrom(WARNING_RESOURCES_PATH);
    }

    // Provides all .faasch files from the invalid test resources directory
    private static Stream<File> invalidTestFiles() throws IOException {
        return getFaaschFilesFrom(INVALID_RESOURCES_PATH);
    }

    @ParameterizedTest
    @MethodSource("validTestFiles")
    void testValidFilesPassChecker(File file) throws Exception {
        Program program = Parser.parseSourceFile(file);

        assertDoesNotThrow(
                () -> globalChecker.check(program),
                "File " + file.getName() + " should be valid and pass checking");
    }

    @ParameterizedTest
    @MethodSource("warningTestFiles")
    void testWarningFilesPassChecker(File file) throws Exception {
        Program program = Parser.parseSourceFile(file);

        CompoundException exception = assertThrows(
                CompoundException.class,
                () -> globalChecker.check(program),
                "File " + file.getName() + " should throw CompoundException containing warnings");

        boolean hasErrors = exception.getCauses().stream()
                .anyMatch(cause -> !(cause instanceof WarningException));

        boolean hasWarnings = exception.getCauses().stream()
                .anyMatch(cause -> cause instanceof WarningException);

        assertFalse(
                hasErrors,
                "File " + file.getName() + " in warning folder should not contain blocking errors");

        assertTrue(
                hasWarnings,
                "File " + file.getName() + " in warning folder should contain at least one warning");
    }

    @ParameterizedTest
    @MethodSource("invalidTestFiles")
    void testInvalidFilesFailChecker(File file) throws Exception {
        Program program = Parser.parseSourceFile(file);

        CompoundException exception = assertThrows(
                CompoundException.class,
                () -> globalChecker.check(program),
                "File " + file.getName() + " should be invalid and throw CompoundException");

        boolean hasActualErrors = exception.getCauses().stream()
                .anyMatch(cause -> !(cause instanceof WarningException));

        assertTrue(
                hasActualErrors,
                "File " + file.getName()
                        + " should contain at least one actual error (non-warning) in CompoundException");
    }
}
