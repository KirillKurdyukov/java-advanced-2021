package info.kgeorgiy.ja.kurdyukov.arrayset;

import java.util.*;
import java.util.function.Function;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final int size;
    private final String ERROR_FOR_METHOD = "This collection is immutable.";
    private final List<? extends E> EMPTY_ELEMENT_DATA = Collections.emptyList();
    private final List<? extends E> arraySet;
    private final Comparator<? super E> comparator;

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException(ERROR_FOR_METHOD);
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException(ERROR_FOR_METHOD);
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException(ERROR_FOR_METHOD);
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(ERROR_FOR_METHOD);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException(ERROR_FOR_METHOD);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(ERROR_FOR_METHOD);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(ERROR_FOR_METHOD);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(ERROR_FOR_METHOD);
    }

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        TreeSet<E> tmp = new TreeSet<>(comparator);
        tmp.addAll(collection);
        arraySet = List.copyOf(new TreeSet<>(tmp));
        this.size = arraySet.size();
        this.comparator = comparator;
    }

    private int posElement(E e, int inArray, int outArray) {
        int pos = Collections.binarySearch(arraySet, e, comparator);
        return (pos >= 0) ? (pos + inArray) : Math.abs(pos) - outArray;
    }

    private int checkInvalidPos(int pos) {
        return (0 <= pos && pos < size) ? pos : -1;
    }

    public int posLower(E e) {
        return checkInvalidPos(posElement(e, -1, 2));
    }

    public int posFloor(E e) {
        return checkInvalidPos(posElement(e, 0, 2));
    }

    public int posCeiling(E e) {
        return checkInvalidPos(posElement(e, 0, 1));
    }

    public int posHigher(E e) {
        return checkInvalidPos(posElement(e, 1, 1));
    }

    private E getElement(E e, Function<E, Integer> function) {
        int pos = function.apply(e);
        return pos == -1 ? null : arraySet.get(pos);
    }

    @Override
    public E lower(E e) {
        return getElement(e, this::posLower);
    }

    @Override
    public E floor(E e) {
        return getElement(e, this::posFloor);
    }

    @Override
    public E ceiling(E e) {
        return getElement(e, this::posCeiling);
    }

    @Override
    public E higher(E e) {
        return getElement(e, this::posHigher);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(arraySet, (E) o, comparator) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return (Iterator<E>) arraySet.iterator();
    }

    private ArraySet(List<? extends E> collection, Comparator<? super E> comparator, boolean privat) {
        this.size = collection.size();
        this.arraySet = collection;
        this.comparator = comparator;
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<E>(new ReverseArrayList<>(arraySet), Collections.reverseOrder(comparator), false);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int l = fromInclusive ? posCeiling(fromElement) : posHigher(fromElement);
        int r = toInclusive ? posFloor(toElement) : posLower(toElement);
        if (l == -1 || r == -1 || r < l)
            return new ArraySet<>(EMPTY_ELEMENT_DATA, comparator);
        else
            return new ArraySet<>(arraySet.subList(l, r + 1), comparator, false);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        if (isEmpty())
            return this;
        return subSet(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        if (isEmpty())
            return this;
        return subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (comparator != null && comparator.compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        } else if (comparator == null && ((Comparator<E>) Comparator.naturalOrder()).compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public E first() {
        if (isEmpty())
            throw new NoSuchElementException();
        return arraySet.get(0);
    }

    @Override
    public E last() {
        if (isEmpty())
            throw new NoSuchElementException();
        return arraySet.get(size - 1);
    }

    public static class ReverseArrayList<E> extends AbstractList<E> {
        private final int size;
        private boolean reverse = true;
        private final List<? extends E> reverseArrayList;

        @SuppressWarnings("unchecked")
        public ReverseArrayList(List<? extends E> reverseArrayList) {
            if (reverseArrayList instanceof ReverseArrayList) {
                ReverseArrayList<? extends E> list = (ReverseArrayList<? extends E>) reverseArrayList;
                this.reverseArrayList = list.reverseArrayList;
                this.reverse = !list.reverse;
            } else {
                this.reverseArrayList = reverseArrayList;
            }
            this.size = reverseArrayList.size();
        }

        @Override
        public E get(int index) {
            return reverse ? reverseArrayList.get(size - 1 - index) : reverseArrayList.get(index);
        }

        @Override
        public int size() {
            return size;
        }
    }
}
