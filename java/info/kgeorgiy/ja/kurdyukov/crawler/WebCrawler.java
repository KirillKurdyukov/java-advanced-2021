package info.kgeorgiy.ja.kurdyukov.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {

    private final ExecutorService downloaderService;

    private final ExecutorService extractorService;

    private final Downloader downloader;

    private final List<Future<?>> arrayFutures = new CopyOnWriteArrayList<>();

    private final int perHost;

    @Override
    public Result download(String s, int i) {
        ConcurrentHashMap<String, IOException> concurrentHashMap = new ConcurrentHashMap<>();
        List<String> document = new CopyOnWriteArrayList<>();
        if (i <= 0)
            throw new IllegalArgumentException("Depth must be larger zero.");
        recursiveDownload(s, i, document, concurrentHashMap);
        arrayFutures.forEach(f -> {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        return new Result(document, concurrentHashMap);
    }

    private void recursiveDownload(String s,
                                   int i,
                                   List<String> documents,
                                   ConcurrentHashMap<String,
                                           IOException> concurrentHashMap) {
        if (i == 1) {
            arrayFutures.add(downloaderService.submit(() -> {
                try {
                    downloader.download(s);
                    documents.add(s);
                } catch (IOException e) {
                    concurrentHashMap.put(s, e);
                }
                documents.add(s);
            }));
        }

        arrayFutures.add(downloaderService.submit(() -> {
            try {
                Document document = downloader.download(s);
                documents.add(s);
                arrayFutures.add(extractorService.submit(() -> {
                    try {
                        document.extractLinks()
                                .forEach((e) -> recursiveDownload(e, i - 1, documents, concurrentHashMap));
                    } catch (IOException e) {
                        concurrentHashMap.put(s, e);
                    }
                }));
            } catch (IOException e) {
                concurrentHashMap.put(s, e);
            }
        }));
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
}
