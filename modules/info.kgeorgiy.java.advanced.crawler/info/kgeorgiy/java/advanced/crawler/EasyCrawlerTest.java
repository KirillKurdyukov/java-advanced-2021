package info.kgeorgiy.java.advanced.crawler;

import info.kgeorgiy.java.advanced.base.BaseTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EasyCrawlerTest extends BaseTest {
    @Test
    public void test01_singlePage() throws IOException {
        test("https://en.itmo.ru/en/page/50/Partnership.htm", 1);
    }

    @Test
    public void test02_pageAndLinks() throws IOException {
        test("https://itmo.ru", 2);
    }

    private static void test(final String url, final int depth) throws IOException {
        final ReplayDownloader replayDownloader = new ReplayDownloader(url, 10, 10);
        final Result actual = download(url, depth, replayDownloader);
        final Result expected = replayDownloader.expected(url, depth);
        checkResult(expected, actual);
    }

    public static void checkResult(final Result expected, final Result actual) {
        compare("Downloaded OK", Set.copyOf(expected.getDownloaded()), Set.copyOf(actual.getDownloaded()));
        compare("Downloaded with errors", expected.getErrors().keySet(), actual.getErrors().keySet());
        Assert.assertEquals("Errors", expected.getErrors(), actual.getErrors());
    }

    private static void compare(final String context, final Set<String> expected, final Set<String> actual) {
        final Set<String> missing = difference(expected, actual);
        final Set<String> excess = difference(actual, expected);
        final String message = String.format("%s:%n    missing = %s%n    excess = %s%n", context, missing, excess);
        Assert.assertTrue(message, missing.isEmpty() && excess.isEmpty());
    }

    private static Result download(final String url, final int depth, final Downloader downloader) {
        final CheckingDownloader checkingDownloader = new CheckingDownloader(downloader, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        try (final Crawler crawler = createInstance(checkingDownloader)) {
            final Result result = crawler.download(url, depth);
            Assert.assertNull(checkingDownloader.getError(), checkingDownloader.getError());
            return result;
        }
    }

    private static Crawler createInstance(final Downloader downloader) {
        try {
            final Constructor<?> constructor = loadClass().getConstructor(Downloader.class, int.class, int.class, int.class);
            return (Crawler) constructor.newInstance(downloader, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    private static Set<String> difference(final Set<String> a, final Set<String> b) {
        return a.stream().filter(o -> !b.contains(o)).collect(Collectors.toSet());
    }
}
