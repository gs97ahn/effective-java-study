package ahn.chapter5.item29.stack;

import org.junit.jupiter.api.Test;

public class GenericStackTest {

    @Test
    void test() {
        // given
        String[] args = new String[3];
        for (int i = 0; i < 3; i++)
            args[i] = String.valueOf((char) ('a' + i));

        // when
        Stack<String> stack = new Stack<>();
        for (String arg : args)
            stack.push(arg);

        // then
        while (!stack.isEmpty())
            System.out.println(stack.pop().toUpperCase());
    }
}
