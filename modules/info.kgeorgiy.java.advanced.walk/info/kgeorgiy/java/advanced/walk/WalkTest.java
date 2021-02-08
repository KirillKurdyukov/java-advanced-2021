package info.kgeorgiy.java.advanced.walk;

import info.kgeorgiy.java.advanced.base.BaseTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Tests for easy version
 * of <a href="https://www.kgeorgiy.info/courses/java-advanced/homeworks.html#homework-walk">Walk</a> homework
 * for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WalkTest extends BaseTest {
    protected static final Path DIR = Path.of("__Test__Walk__");
    private static final String ENGLISH_DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    protected static final Random RANDOM = new Random(23084701432182342L);

    @Rule
    public TestName name = new TestName();

    private Path getTestDir() {
        return DIR.resolve(name.getMethodName());
    }

    @Test
    public void test10_oneEmptyFile() throws IOException {
        test(randomFiles(1, 0));
    }

    @Test
    public void test20_smallRandomFiles() throws IOException {
        test(randomFiles(10, 100));
    }

    @Test
    public void test60_noInput() {
        runRaw(randomFileName(), randomFileName());
    }

    private Map<String, String> randomFiles(final int n, final int maxL) throws IOException {
        return randomFiles(n, maxL, getTestDir());
    }

    protected void test(final Map<String, String> files) {
        test(files.keySet(), files);
    }

    protected void test(final Collection<String> inputs, final Map<String, String> files) {
        final Path inputFile = DIR.resolve(name.getMethodName() + ".in");
        final Path outputFile = DIR.resolve(name.getMethodName() + ".out");
        try {
            Files.writeString(inputFile, generateInput(inputs));
        } catch (final IOException e) {
            throw new AssertionError("Cannot write input file " + inputFile);
        }
        run(inputFile, outputFile);
        try {
            for (final String line : Files.readAllLines(outputFile, StandardCharsets.UTF_8)) {
                final String[] parts = line.split(" ", 2);
                Assert.assertEquals("Invalid line format\n" + line, 2, parts.length);
                Assert.assertTrue("Unexpected file " + parts[1], files.containsKey(parts[1]));
                Assert.assertEquals("Wrong hash", files.remove(parts[1]), parts[0]);
            }
        } catch (final IOException e) {
            throw new AssertionError("Cannot write output file " + outputFile);
        }

        Assert.assertTrue("Some files missing: \n    " + String.join("\n    ", files.keySet()), files.isEmpty());
    }

    private void run(final Path inputFile, final Path outputFile) {
        runRaw(inputFile.toString(), outputFile.toString());
    }

    private void runRaw(final String... args) {
        final Method method;
        final Class<?> cut = loadClass();
        try {
            method = cut.getMethod("main", String[].class);
        } catch (final NoSuchMethodException e) {
            throw new AssertionError("Cannot find method main(String[]) of " + cut, e);
        }
        System.out.println("Running " + name.getMethodName());
        try {
            method.invoke(null, (Object) args);
        } catch (final IllegalAccessException e) {
            throw new AssertionError("Cannot call main(String[]) of " + cut, e);
        } catch (final InvocationTargetException e) {
            throw new AssertionError("Error thrown", e.getCause());
        }
    }

    private static String generateInput(final Collection<String> files) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter);
        files.forEach(writer::println);
        writer.close();
        return stringWriter.toString();
    }

    protected static Map<String, String> randomFiles(final int n, final int maxL, final Path dir) throws IOException {
        Files.createDirectories(dir);
        final Map<String, String> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            final String name = randomFileName();
            final Path file = dir.resolve(name);
            final byte[] bytes = new byte[RANDOM.nextInt(maxL + 1)];
            RANDOM.nextBytes(bytes);
            Files.write(file, bytes);
            result.put(file.toString(), hash(bytes));
        }
        return result;
    }

    protected static String randomFileName() {
        return RANDOM.ints(30, 0, ENGLISH_DIGITS.length())
                .mapToObj(i -> ENGLISH_DIGITS.substring(i, i + 1))
                .collect(Collectors.joining());
    }

    public static long hash(final byte[] bytes, final int size, long start) {
        for (int i = 0; i < size; i++) {
            start = (start << 8) + (bytes[i] & 0xff);
            final long high = start & 0xff00_0000_0000_0000L;
            if (high != 0) {
                start ^= high >> 48;
                start &= ~high;
            }
        }
        return start;
    }

    private static String hash(final byte[] bytes) {
        return String.format("%016x", hash(bytes, bytes.length, 0));
    }
}
