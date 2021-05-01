package info.kgeorgiy.ja.kurdyukov.concurrent;

import java.util.List;

public class ListResult<R> {
    private final List<R> list;
    private int countElement;
    private boolean wasException;
    private RuntimeException runtimeException;

    public ListResult(List<R> list) {
        this.list = list;
    }

    public synchronized void set(int i, R el) {
        list.set(i, el);
        counting();
        if (countElement == list.size())
            notify();
    }

    public synchronized List<R> getResult() throws InterruptedException {
        while (countElement != list.size())
            wait();

        if (wasException)
            throw runtimeException;

        return list;
    }

    public void counting() {
        countElement++;
    }

    public void setException(RuntimeException r) {
        wasException = true;
        this.runtimeException = r;
    }

    public boolean getException() {
        return wasException;
    }

    public void addException(RuntimeException r) {
        this.runtimeException.addSuppressed(r);
    }
}