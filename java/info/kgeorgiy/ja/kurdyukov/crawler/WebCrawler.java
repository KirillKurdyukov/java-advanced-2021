package info.kgeorgiy.ja.kurdyukov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WebCrawler implements Crawler {

    private final Map<String, SemaphoreQueue> hostMap = new ConcurrentHashMap<>();

    private final ExecutorService downloaderService;

    private final ExecutorService extractorService;

    private final Downloader downloader;

    private final int perHost;

    private class SemaphoreQueue {
        private final Queue<Runnable> requests = new LinkedList<>();
        int counter;

        public SemaphoreQueue(String s) {
        }

        synchronized void work() {
            if (counter < perHost && !requests.isEmpty()) {
                counter++;
                downloaderService.submit(() -> {
                    Objects.requireNonNull(requests.poll()).run();
                    next();
                });
            }
        }

        synchronized void add(Runnable runnable) {
            requests.add(runnable);
            work();
        }

        void next() {
            counter--;
            work();
        }
    }

    /**
     * Downloads pages recursively.
     * @param s is tree root.
     * @param i is depth tree.
     * @return {@link Result}
     */
    @Override
    public Result download(String s, int i) {
        Set<String> urls = ConcurrentHashMap.newKeySet();
        Map<String, IOException> errors = new ConcurrentHashMap<>();
        bfsLevel(s, i, urls, errors);
        return new Result(new ArrayList<>(urls), errors);
    }

    private static class Layer {
        private Set<String> layer = new HashSet<>();

        Layer(String url) {
            layer.add(url);
        }

        void filter(Set<String> urls) {
            layer = layer.stream()
                    .filter(urls::add)
                    .collect(Collectors.toSet());
        }

    }

    private void bfsLevel(String s,
                          int level,
                          Set<String> urls,
                          Map<String, IOException> errors) {
        Layer currentLayer = new Layer(s);
        IntStream.range(0, level)
                .forEach((i) -> {
                    Phaser phaser = new Phaser(1);
                    currentLayer.filter(urls);
                    Set<String> resCur = ConcurrentHashMap.newKeySet();
                    currentLayer.layer.forEach(l -> doDownload(l, resCur, errors, phaser));
                    phaser.arriveAndAwaitAdvance();
                    currentLayer.layer = resCur;
                });
        urls.removeAll(errors.keySet());
    }

    private void doExtract(Document document,
                           String url,
                           Set<String> result,
                           Map<String, IOException> errors,
                           Phaser phaserLevel) {
        try {
            result.addAll(document.extractLinks());
        } catch (IOException e) {
            errors.put(url, e);
        } finally {
            phaserLevel.arrive();
        }
    }

    private void doDownload(String url,
                            Set<String> result,
                            Map<String, IOException> errors,
                            Phaser phaserLevel) {
        final Runnable runnable = () -> {
            try {
                Document document = downloader.download(url);
                phaserLevel.register();
                extractorService.submit(() -> doExtract(document,
                        url,
                        result,
                        errors,
                        phaserLevel)
                );

            } catch (IOException e) {
                errors.put(url, e);
            } finally {
                phaserLevel.arrive();
            }
        };
        try {
            SemaphoreQueue queue = hostMap.computeIfAbsent(URLUtils.getHost(url), SemaphoreQueue::new);
            phaserLevel.register();
            queue.add(runnable);
        } catch (MalformedURLException e) {
            errors.put(url, e);
        }
    }

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;
        this.downloaderService = Executors.newFixedThreadPool(downloaders);
        this.extractorService = Executors.newFixedThreadPool(extractors);
    }

    @Override
    public void close() {
        extractorService.shutdown();
        downloaderService.shutdown();
    }

    /**
     * When loaded, the traversal recursively accepts command-like
     * arguments when used correctly: WebCrawler URL [depth [downloads [extractors [perHost]]]]
     * @param args is command line arguments.
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length > 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Correct usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
        }
        Function<Integer, Integer> index = (i) -> (i < args.length) ? Integer.parseInt(args[i]) : 5;
        String url = args[0];
        int depth = index.apply(1),
                downloads = index.apply(2),
                extractors = index.apply(3),
                perHost = index.apply(4);
        try (Crawler crawler = new WebCrawler(null, downloads, extractors, perHost)) {
            Result result = crawler.download(url, depth);
            result.getDownloaded().forEach(i -> System.out.println("URL: " + i));
            result.getErrors().forEach((key, value) -> System.out.println("URL: " + key + "\nError: " + value));
        } catch (Exception e) {
            System.out.println("Crawler failed: " + e.getMessage());
        }
    }
}
