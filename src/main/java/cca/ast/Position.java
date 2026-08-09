package cca.ast;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Position {
    private final int line; // 1..n
    private final int column; // 1..n
    private final String sourceFile;

    public Position(String sourceFile, int line, int column) {
        this.line = line;
        this.column = column;
        this.sourceFile = sourceFile;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    public String sourceFile() {
        return sourceFile;
    }

    public String formattedPosition() {
        if (sourceFile == null) {
            return String.format("line %d column %d", line, column);
        } else {
            Path absolutePath = Paths.get(sourceFile).toAbsolutePath();

            // gets the current path to save the relative path
            Path relativePath = Paths.get(".").toAbsolutePath().relativize(absolutePath);
            return String.format("file '%s' line %d column %d", relativePath, line, column);
        }
    }

    public String toString() {
        return formattedPosition();
    }

}
