package info.kgeorgiy.ja.kurdyukov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ParallelMapperImpl implements ParallelMapper {

    private final Queue<Runnable> requests = new LinkedList<>();
    private final List<Thread> threads;

    private Runnable poll() throws InterruptedException {
        synchronized (requests) {
            while (requests.isEmpty()) {
                requests.wait();
            }
            return requests.poll();
        }
    }

    private void pushRequest(Runnable runnable) {
        synchronized (requests) {
            requests.add(runnable);
            requests.notify();
        }
    }

    public ParallelMapperImpl(int threadsSize) {
        final Runnable runnable = () -> {
            try {
                while (!Thread.interrupted())
                    Objects.requireNonNull(poll()).run();
            } catch (InterruptedException ignored) {
                /* ignored */
            } finally {
                Thread.currentThread().interrupt();
            }
        };
        this.threads = Stream.generate(() -> new Thread(runnable))
                .limit(threadsSize)
                .collect(Collectors.toList());
        threads.forEach(Thread::start);
    }

    /**
     * Applies a function {@link Function} to all elements of the specified list {@link List}.
     * @param function is function {@link Function} to apply
     * @param list is list {@link List} to process
     * @param <T> is type the specified list.
     * @param <R> is type result list.
     * @return result list when any thread is interrupted.
     * @throws InterruptedException when any thread is interrupted.
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        ListResult<R> result = new ListResult<>(new ArrayList<>(Collections.nCopies(list.size(), null)));
        IntStream.range(0, list.size())
                .forEach((i) -> {
                            try {
                                pushRequest(() -> result
                                        .set(i, function
                                                .apply(list.get(i))
                                        )
                                );
                            } catch (RuntimeException e) {
                                synchronized (result) {
                                    if (result.getException())
                                        result.addException(e);
                                    else
                                        result.setException(e);
                                    result.counting();
                                }
                            }
                        }
                );
        return result.getResult();
    }

    /**
     * Closes created threads.
     */
    @Override
    public void close() {
        synchronized (threads) {
            threads.forEach(Thread::interrupt);
            IterativeParallelism.joinThreads(threads);
        }
    }

}
