package io.github.kjens93.conversations.collections;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by kjensen on 11/27/16.
 */
public class NotifyingQueue<T> implements Queue<T> {

    private final Queue<T> source;

    public NotifyingQueue() {
        this(new LinkedList<>());
    }

    public NotifyingQueue(Collection<T> source) {
        this(new LinkedList<>(source));
    }

    public NotifyingQueue(Queue<T> source) {
        this.source = source;
    }

    @Override
    public synchronized boolean add(T t) {
        boolean result = source.add(t);
        this.notifyAll();
        return result;
    }

    @Override
    public boolean offer(T t) {
        return source.offer(t);
    }

    @Override
    public T remove() {
        return source.remove();
    }

    @Override
    public T poll() {
        return source.poll();
    }

    @Override
    public T element() {
        return source.element();
    }

    @Override
    public T peek() {
        return source.peek();
    }

    @Override
    public int size() {
        return source.size();
    }

    @Override
    public boolean isEmpty() {
        return source.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return source.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return source.iterator();
    }

    @Override
    public Object[] toArray() {
        return source.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return source.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return source.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return source.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return source.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return source.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return source.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return source.retainAll(c);
    }

    @Override
    public void clear() {
        source.clear();
    }

    @Override
    public boolean equals(Object o) {
        return source.equals(o);
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }

    @Override
    public Spliterator<T> spliterator() {
        return source.spliterator();
    }

    @Override
    public Stream<T> stream() {
        return source.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return source.parallelStream();
    }

}
