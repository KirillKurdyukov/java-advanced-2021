package info.kgeorgiy.ja.kurdyukov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WebCrawler implements Crawler {

    private final ExecutorService downloaderService;

    private final ExecutorService extractorService;

    private final Downloader downloader;

    private final int perHost;

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
        phaserLevel.register();
        downloaderService.submit(runnable);
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

    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length > 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Correct usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
        }
        Function<Integer, Integer> index = (i) -> (i < args.length) ? Integer.parseInt(args[i]) : 5;
        final String url = args[0];
        final int depth = index.apply(1),
                downloads = index.apply(2),
                extractors = index.apply(3),
                perHost = index.apply(4);
        try (final Crawler crawler = new WebCrawler(null, downloads, extractors, perHost)) {
            final Result result = crawler.download(url, depth);
            result.getDownloaded().forEach(i -> System.out.println("URL: " + i));
            result.getErrors().forEach((key, value) -> System.out.println("URL: " + key + "\nError: " + value));
        } catch (final Exception e) {
            System.out.println("Crawler failed: " + e.getMessage());
        }
    }
}
