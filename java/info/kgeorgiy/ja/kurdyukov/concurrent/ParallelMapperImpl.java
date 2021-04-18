package info.kgeorgiy.ja.kurdyukov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ParallelMapperImpl implements ParallelMapper {

    final Queue<Runnable> requests = new LinkedList<>();
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

    private static class MapperResult<R> {
        private final List<R> list;
        private int countElement;

        public MapperResult(List<R> list) {
            this.list = list;
        }

        public synchronized void set(int i, R el) {
            list.set(i, el);
            countElement++;
            if (countElement == list.size())
                notify();
        }

        public synchronized List<R> getResult() throws InterruptedException {
            while (countElement != list.size())
                wait();
            return list;
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        MapperResult<R> result = new MapperResult<>(new ArrayList<>(Collections.nCopies(list.size(), null)));
        IntStream.range(0, list.size())
                .forEach(i -> pushRequest(() -> result
                                .set(i, function
                                        .apply(list.get(i))
                                )
                        )
                );
        return result.getResult();
    }

    @Override
    public void close() {
        threads.forEach(Thread::interrupt);
        IterativeParallelism.joinThreads(threads);
    }

    /*
    public static void main(String[] args) throws InterruptedException {
        ParallelMapperImpl p = new ParallelMapperImpl(3);
        List<Integer> list = List.of(3, 5, 6, 6, 1, 1,1, 1,1 ,1, 1);

        try {
            System.out.println(p.map(x -> x * x, list).toString());
            System.out.println(new IterativeParallelism(p).maximum(3, list, new Comparator<Integer>() {
                @Override
                public int compare(final Integer a, final Integer b) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                    System.out.println(Thread.currentThread().getName() + " " + a + " " + b);

                    return a.compareTo(b);
                }
            }).toString());
            p.close();
        } catch (InterruptedException ignored) {
        }
    } */
}
