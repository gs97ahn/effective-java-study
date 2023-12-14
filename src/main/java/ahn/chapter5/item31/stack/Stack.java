package ahn.chapter5.item31.stack;

import java.util.Arrays;
import java.util.Collection;
import java.util.EmptyStackException;

public class Stack<E> {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public void pushAll(Iterable<? extends E> src) {
        for (E e : src)
            push(e);
    }

    public E pop() {
        if (size == 0)
            throw new EmptyStackException();

        @SuppressWarnings("unchekced") E result = (E) elements[--size];

        elements[size] = null;
        return result;
    }

    public void popAll(Collection<? super E> dst) {
        while (isEmpty())
            dst.add(pop());
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }

}
