package ahn.chapter8.item53.varargs;

import org.junit.jupiter.api.Test;

public class VarargsTest {

    static int sum(int... args) {
        int sum = 0;
        for (int arg : args)
            sum += arg;
        return sum;
    }

    @Test
    void sumTest() {
        System.out.println(sum(1, 2, 3));
        System.out.println(sum());
    }

    static int min(int... args) {
        if (args.length == 0)
            throw new IllegalArgumentException("인수가 1개 이상 필요합니다.");
        int min = args[0];
        for (int i = 1; i < args.length; i++)
            if (args[i] < min)
                min = args[i];
        return min;
    }

    @Test
    void minTest() {
        System.out.println(min());
    }

    public void foo() { }
    public void foo(int a1) { }
    public void foo(int a1, int a2) { }
    public void foo(int a1, int a2, int a3) { }
    public void foo(int a1, int a2, int a3, int... rest) { }
}
