package info.kgeorgiy.ja.kurdyukov.walk;

import java.io.*;
import java.nio.file.*;

public class RecursiveWalk {

    public static final String FAIL_HASH = "0000000000000000 ";
    private Path inputPath;
    private Path outputPath;

    private Path setPath(String path, String errorMessage) throws RecursiveWalkException {
        try {
            return Paths.get(path);
        } catch (InvalidPathException e) {
            throw new RecursiveWalkException(errorMessage);
        }
    }

    public void setInputPath(String inputPath) throws RecursiveWalkException {
        this.inputPath = setPath(inputPath, "Invalid path for input file.");
    }

    public void setOutputPath(String outputPath) throws RecursiveWalkException {
        this.outputPath = setPath(outputPath, "Invalid path for output file.");
        try {
            if (this.outputPath.getParent() == null) {
                Files.createDirectories(this.outputPath);
            }
        } catch (IOException e) {
            throw new RecursiveWalkException("Can't create directory for file. ");
        }
    }

    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2 ||
                    args[0] == null || args[1] == null
                    || args[0].isEmpty() || args[1].isEmpty()) {
                throw new RecursiveWalkException("Incorrect arguments start program.");
            }

            RecursiveWalk recursiveWalk = new RecursiveWalk();
            recursiveWalk.setInputPath(args[0]);
            recursiveWalk.setOutputPath(args[1]);

            recursiveWalk.recursiveWalk();

        } catch (RecursiveWalkException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void recursiveWalk() throws RecursiveWalkException {
        try (BufferedReader bufferedReader = Files.newBufferedReader(this.inputPath)) {
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(this.outputPath)) {
                String currentPath;
                HashFileVisitor hashFileVisitor = new HashFileVisitor(bufferedWriter);
                try {
                    while (true) {
                        try {
                            currentPath = bufferedReader.readLine();
                        } catch (IOException e) {
                            throw new RecursiveWalkException("Error while reading file. " + e.getMessage());
                        }
                        if (currentPath == null)
                            break;
                        try {
                            Files.walkFileTree(Paths.get(currentPath), hashFileVisitor);
                        } catch (InvalidPathException | IOException e) {
                            bufferedWriter.write(FAIL_HASH + currentPath);
                            bufferedWriter.newLine();
                        }
                    }
                } catch (IOException e) {
                    throw new RecursiveWalkException("Error while writing file. " + e.getMessage());
                }
            } catch (AccessDeniedException e) {
                throw new RecursiveWalkException("Access denied to output file. " + e.getMessage());
            } catch (NoSuchFileException e) {
                throw new RecursiveWalkException("Output file not found. " + e.getMessage());
            } catch (IOException e) {
                throw new RecursiveWalkException("Output file error. " + e.getMessage());
            }
        } catch (AccessDeniedException e) {
            throw new RecursiveWalkException("Access denied to input file. " + e.getMessage());
        } catch (NoSuchFileException e) {
            throw new RecursiveWalkException("Input file not found. " + e.getMessage());
        } catch (IOException e) {
            throw new RecursiveWalkException("Input file error. " + e.getMessage());
        }
    }
}
