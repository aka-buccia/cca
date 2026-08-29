package cca.checker;

import cca.ast.Program;
import cca.exceptions.CompoundException;
import cca.parser.Parser;
import org.junit.jupiter.api.DisplayName;
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

class CheckerTest {

    private static final Path VALID_RESOURCES_PATH = Paths.get("src/test/resources/checker/valid");
    private static final Path INVALID_RESOURCES_PATH = Paths.get("src/test/resources/checker/invalid");

    private final GlobalChecker globalChecker = new GlobalChecker();

    // Provides all .faasch files from the valid test resources directory
    private static Stream<File> validTestFiles() throws IOException {
        return Files.list(VALID_RESOURCES_PATH)
                .filter(path -> path.toString().endsWith(".faasch"))
                .map(Path::toFile);
    }

    // Provides all .faasch files from the invalid test resources directory
    private static Stream<File> invalidTestFiles() throws IOException {
        return Files.list(INVALID_RESOURCES_PATH)
                .filter(path -> path.toString().endsWith(".faasch"))
                .map(Path::toFile);
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
    @MethodSource("invalidTestFiles")
    void testInvalidFilesFailChecker(File file) throws Exception {
        Program program = Parser.parseSourceFile(file);

        CompoundException exception = assertThrows(
                CompoundException.class,
                () -> globalChecker.check(program),
                "File " + file.getName() + " should be invalid and throw CompoundException");

        assertFalse(
                exception.getCauses().isEmpty(),
                "File " + file.getName() + " should contain at least one cause in CompoundException");
    }
}
