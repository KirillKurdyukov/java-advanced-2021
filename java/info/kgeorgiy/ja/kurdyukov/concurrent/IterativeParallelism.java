package info.kgeorgiy.ja.kurdyukov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IterativeParallelism implements ListIP {

    private <T, U> List<U> getParallelResult(int threadSize,
                                             List<? extends T> list,
                                             Function<List<? extends T>, U> function) throws InterruptedException {
        int blockSize = list.size() / threadSize;
        int remainder = list.size() % threadSize;
        threadSize = Math.min(threadSize, list.size());
        List<U> resultParallel = new ArrayList<>(Collections.nCopies(threadSize, null));
        List<Thread> threads = new ArrayList<>();
        int left, right = 0;
        for (int i = 0; i < threadSize; i++) {
            left = right;
            right = left + blockSize + (remainder-- > 0 ? 1 : 0);
            int finalI = i;
            int finalLeft = left;
            int finalRight = right;
            Thread thread = new Thread(() -> {
                resultParallel.set(finalI,
                        function.apply(list.subList(finalLeft, finalRight)));
            });
            thread.start();
            threads.add(thread);
        }
        for (var thread : threads)
            thread.join();
        return resultParallel;
    }

    @Override
    public String join(int threadsSize, List<?> list) throws InterruptedException {
        return String.join("", getParallelResult(threadsSize,
                list,
                l -> l.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining())));
    }

    @Override
    public <T> List<T> filter(int threadsSize, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return getParallelResult(threadsSize,
                list,
                l -> l.stream()
                        .filter(predicate))
                .stream()
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public <T, U> List<U> map(int threadsSize, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return getParallelResult(threadsSize,
                list,
                l -> l.stream()
                        .map(function))
                .stream()
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public <T> T maximum(int threadSize, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return getParallelResult(threadSize,
                list,
                l -> l.stream()
                        .max(comparator)
                        .orElseThrow())
                .stream()
                .max(comparator)
                .orElseThrow();
    }

    @Override
    public <T> T minimum(int threadSize, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threadSize, list, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threadSize, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return getParallelResult(threadSize,
                list,
                l -> l.stream()
                        .allMatch(predicate))
                .stream()
                .reduce(Boolean::logicalAnd)
                .orElseThrow();
    }

    @Override
    public <T> boolean any(int threadSize, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return getParallelResult(threadSize,
                list,
                l -> l.stream()
                        .anyMatch(predicate))
                .stream()
                .reduce(Boolean::logicalOr)
                .orElseThrow();
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println(new IterativeParallelism().maximum(4, List.of(2, 4, 1, 5, 6), Integer::compareTo));
    }
}
