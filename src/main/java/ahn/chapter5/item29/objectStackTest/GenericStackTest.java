package ahn.chapter5.item29.objectStackTest;

import org.junit.jupiter.api.Test;

public class ObjectStackTest {

    @Test
    void test() {
        String[] args = new String[3];

        Stack<Object> stack = new Stack<>();
        for (String arg : args)
            stack.push(arg);
        while (!stack.isEmpty())
            System.out.println(stack.pop().toUpperCase());
    }
}
