import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class CodePrinter {
    public static void main(String[] args) {
        // Default to current directory if no argument provided
        String rootPath = args.length > 0 ? args[0] : ".";
        String outputFile = "project_code.txt";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            printAllJavaFiles(rootPath, writer);
            System.out.println("Code has been written to " + outputFile);
        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
    }

    private static void printAllJavaFiles(String rootPath, PrintWriter writer) throws IOException {
        Path startPath = Paths.get(rootPath);
        
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".java")) {
                    writer.println("\n=== File: " + file.toString() + " ===");
                    try {
                        String content = Files.readString(file);
                        writer.println(content);
                    } catch (IOException e) {
                        writer.println("Error reading file: " + e.getMessage());
                    }
                    writer.println("====================\n");
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                writer.println("Failed to access: " + file + " - " + exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });
    }
}