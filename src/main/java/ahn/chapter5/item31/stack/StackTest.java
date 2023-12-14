package ahn.chapter5.item31.stack;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

public class StackTest {

    @Test
    void pushAllTest() {
        // given
        Stack<Number> stack = new Stack<>();
        Iterable<Integer> integers = List.of(1, 2, 3);

        // when
        stack.pushAll(integers);
    }

    @Test
    void popAllTest() {
        // given
        Stack<Number> stack = new Stack<>();
        Collection<Object> objects = List.of(1, 2, 3);

        // when
        stack.popAll(objects);
    }
}
