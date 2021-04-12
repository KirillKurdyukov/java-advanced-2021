package info.kgeorgiy.ja.kurdyukov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                U resCurrent = function.apply(list.subList(finalLeft, finalRight));
                synchronized (resultParallel) {
                    resultParallel.set(finalI, resCurrent);
                }
            });
            thread.start();
            threads.add(thread);
        }
        for (var thread : threads)
            thread.join();
        return resultParallel;
    }

    /**
     * Creates and returns a string of elements in the specified list.
     *
     * @param threadsSize is maximum number of created threads to implement the method.
     * @param list is list of elements.
     * @return {@link String} is element string.
     * @throws InterruptedException when any thread is interrupted.
     */
    @Override
    public String join(int threadsSize, List<?> list) throws InterruptedException {
        return String.join("", getParallelResult(threadsSize,
                list,
                l -> l.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining())));
    }

    /**
     * Filters the specified list.
     *
     * @param threadsSize is maximum number of created threads to implement the method.
     * @param list is list of elements.
     * @param predicate is filter predicate.
     * @param <T> is specified list type.
     * @return filtered list.
     * @throws InterruptedException when any thread is interrupted.
     */
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

    /**
     * Applies the specified function to all elements in the specified list and returns a new list.
     *
     * @param threadsSize is maximum number of created threads to implement the method.
     * @param list is list of elements.
     * @param function is function for element. <i>T -> U</i>
     * @param <T> is type of specified list.
     * @param <U> is type of new list.
     * @return new list.
     * @throws InterruptedException when any thread is interrupted.
     */
    @Override
    public <T, U> List<U> map(int threadsSize, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return getParallelResult(threadsSize,
                list,
                l -> l.stream()
                        .map(function))
                .stream()
                .flatMap(l -> l)
                .collect(Collectors.toList());
    }

    /**
     * Finds the maximum element in the specified list.
     *
     * @param threadSize is maximum number of created threads to implement the method.
     * @param list is list of elements.
     * @param comparator is comparator for comparison elements.
     * @param <T> is type of specified list.
     * @return maximum element.
     * @throws InterruptedException when any thread is interrupted.
     */
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

    /**
     * Finds the minimum element in the specified list.
     *
     * @param threadSize is minimum number of created threads to implement the method.
     * @param list is list of elements.
     * @param comparator is comparator for comparison elements.
     * @param <T> is type of specified list.
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
     * @param list is list of elements.
     * @param predicate is predicate for check.
     * @param <T> is type of specified list.
     * @return result check is true or false.
     * @throws InterruptedException when any thread is interrupted.
     */
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

    /**
     * Checks if any the elements of the specified list match a predicate.
     *
     * @param threadSize is maximum number of created threads to implement the method.
     * @param list is list of elements.
     * @param predicate is predicate for check.
     * @param <T> is type of specified list.
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
