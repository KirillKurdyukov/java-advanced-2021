package info.kgeorgiy.ja.kurdyukov.walk;


import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class HashFileVisitor extends SimpleFileVisitor<Path> {
    private final BufferedWriter bufferedWriter;
    private final long PJW_CONST = 0xFF00000000000000L;
    private final byte[] buffer = new byte[1024];

    public HashFileVisitor(BufferedWriter bufferedWriter) {
        this.bufferedWriter = bufferedWriter;
    }

    private long getValuePJW(long h, int size) {
        long high;
        for (int i = 0; i < size; i++) {
            h = (h << 8) + (buffer[i] & 0xFF);
            if ((high = h & PJW_CONST) != 0) {
                h ^= high >> 48;
            }
            h &= ~high;
        }
        return h;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        long h = 0;
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(file))) {
            int size;
            while ((size = inputStream.read(buffer)) != -1) {
                h = getValuePJW(h, size);
            }
            bufferedWriter.write(String.format("%016x", h) + " " + file);
        } catch (IOException e) {
            bufferedWriter.write(String.format("%016x", h) + " " + file);
        }
        bufferedWriter.newLine();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        bufferedWriter.write(RecursiveWalk.FAIL_HASH + file);
        bufferedWriter.newLine();
        return FileVisitResult.CONTINUE;
    }
}
