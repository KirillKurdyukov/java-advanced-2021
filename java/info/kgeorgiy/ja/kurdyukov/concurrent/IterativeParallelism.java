package info.kgeorgiy.ja.kurdyukov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {

    private ParallelMapper mapper;

    public IterativeParallelism() {

    }

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    private <T> List<List<? extends T>> getBlocks(int threadSize, List<? extends T> list) {
        List<List<? extends T>> lists = new ArrayList<>();
        int blockSize = list.size() / threadSize;
        int remainder = list.size() % threadSize;
        threadSize = Math.min(threadSize, list.size());
        int left, right = 0;
        for (int i = 0; i < threadSize; i++) {
            left = right;
            right = left + blockSize + (remainder-- > 0 ? 1 : 0);
            int finalLeft = left;
            int finalRight = right;
            lists.add(list.subList(finalLeft, finalRight));
        }
        return lists;
    }

    private <T, U, R> R getParallelResult(int threadSize,
                                          List<? extends T> list,
                                          Function<List<? extends T>, U> function,
                                          Function<List<U>, R> reduceFunction) throws InterruptedException {
        List<List<? extends T>> blocks = getBlocks(threadSize, list);
        List<Thread> threads = new ArrayList<>();
        List<U> resultParallel = new ArrayList<>(Collections.nCopies(threadSize, null));
        if (mapper == null) {
            List<U> finalResultParallel = resultParallel;
            IntStream.range(0, threadSize).forEach(i -> {
                Thread thread = new Thread(() -> {
                    U resCurrent = function.apply(blocks.get(i));
                    finalResultParallel.set(i, resCurrent);
                });
                thread.start();
                threads.add(thread);
            });
            joinThreads(threads);
        } else {
            try {
                resultParallel = mapper.map(function, blocks);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return reduceFunction.apply(resultParallel);
    }

    public static void joinThreads(List<Thread> threads) {
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        });
    }

    /**
     * Creates and returns a string of elements in the specified list.
     *
     * @param threadsSize is maximum number of created threads to implement the method.
     * @param list        is list of elements.
     * @return {@link String} is element string.
     * @throws InterruptedException when any thread is interrupted.
     */
    @Override
    public String join(int threadsSize, List<?> list) throws InterruptedException {
        return getParallelResult(threadsSize,
                list,
                l -> l.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining()),
                l -> String.join("", l));
    }

    private <T, U> List<U> noMonoid(int threadsSize,
                                    List<? extends T> list,
                                    Function<Stream<? extends T>, Stream<? extends U>> function) throws InterruptedException {
        return getParallelResult(threadsSize,
                list,
                l -> function.apply(l.stream()),
                l -> l.stream()
                        .flatMap(Function.identity())
                        .collect(Collectors.toList()));
    }

    /**
     * Filters the specified list.
     *
     * @param threadsSize is maximum number of created threads to implement the method.
     * @param list        is list of elements.
     * @param predicate   is filter predicate.
     * @param <T>         is specified list type.
     * @return filtered list.
     * @throws InterruptedException when any thread is interrupted.
     */
    @Override
    public <T> List<T> filter(int threadsSize, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return noMonoid(threadsSize,
                list,
                l -> l.filter(predicate));
    }

    /**
     * Applies the specified function to all elements in the specified list and returns a new list.
     *
     * @param threadsSize is maximum number of created threads to implement the method.
     * @param list        is list of elements.
     * @param function    is function for element. <i>T -> U</i>
     * @param <T>         is type of specified list.
     * @param <U>         is type of new list.
     * @return new list.
     * @throws InterruptedException when any thread is interrupted.
     */
    @Override
    public <T, U> List<U> map(int threadsSize, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return noMonoid(threadsSize,
                list,
                l -> l.map(function));
    }

    /**
     * Finds the maximum element in the specified list.
     *
     * @param threadSize is maximum number of created threads to implement the method.
     * @param list       is list of elements.
     * @param comparator is comparator for comparison elements.
     * @param <T>        is type of specified list.
     * @return maximum element.
     * @throws InterruptedException when any thread is interrupted.
     */
    @Override
    public <T> T maximum(int threadSize, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return getParallelResult(threadSize,
                list,
                l -> l.stream()
                        .max(comparator)
                        .orElseThrow(),
                l -> l.stream()
                        .max(comparator)
                        .orElseThrow());
    }

    /**
     * Finds the minimum element in the specified list.
     *
     * @param threadSize is minimum number of created threads to implement the method.
     * @param list       is list of elements.
     * @param comparator is comparator for comparison elements.
     * @param <T>        is type of specified list.
     * @return minimum element.
     * @throws InterruptedException when any thread is interrupted.
     */
    @Override
    public <T> T minimum(int threadSize, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threadSize, list, comparator.reversed());
    }

    /**
     * Checks if all the elements of the specified list match a predicate.
     *
     * @param threadSize is maximum number of created threads to implement the method.
     * @param list       is list of elements.
     * @param predicate  is predicate for check.
     * @param <T>        is type of specified list.
     * @return result check is true or false.
     * @throws InterruptedException when any thread is interrupted.
     */
    @Override
    public <T> boolean all(int threadSize, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return getParallelResult(threadSize,
                list,
                l -> l.stream()
                        .allMatch(predicate),
                l -> l.stream()
                        .allMatch(Boolean::booleanValue));
    }

    /**
     * Checks if any the elements of the specified list match a predicate.
     *
     * @param threadSize is maximum number of created threads to implement the method.
     * @param list       is list of elements.
     * @param predicate  is predicate for check.
     * @param <T>        is type of specified list.
     * @return result check is true or false.
     * @throws InterruptedException when any thread is interrupted.
     */
    @Override
    public <T> boolean any(int threadSize, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threadSize, list, predicate.negate());

    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println(new IterativeParallelism().maximum(4, List.of(2, 4, 1, 5, 6), Integer::compareTo));
    }
}
