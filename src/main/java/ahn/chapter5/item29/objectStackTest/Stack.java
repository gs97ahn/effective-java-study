package ahn.chapter5.item29.objectStackTest;

import java.util.Arrays;
import java.util.EmptyStackException;

public class Stack<E> {
//    private E[] elements;
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

//    public Stack() {
//        elements = new E[DEFAULT_INITIAL_CAPACITY];
//    }

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

//    public void push(Object e) {
//        ensureCapacity();
//        elements[size++] = e;
//    }

//    public void push(E e) {
//        ensureCapacity();
//        elements[size++] = e;
//    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

//    public Object pop() {
//        if (size == 0)
//            throw new EmptyStackException();
//        Object result = elements[--size];
//        elements[size] = null; // 다 쓴 참조 해제
//        return result;
//    }

//    public E pop() {
//        if (size == 0)
//            throw new EmptyStackException();
//        E result = elements[--size];
//        elements[size] = null; // 다 쓴 참조 해제
//        return result;
//    }

    public E pop() {
        if (size == 0)
            throw new EmptyStackException();

        // push에서 E 타입만 허용하므로 이 형변환은 안전하다.
        @SuppressWarnings("unchekced") E result = (E) elements[--size];

        elements[size] = null; // 다 쓴 참조 해제
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}
