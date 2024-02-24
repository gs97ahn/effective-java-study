package ahn.chapter6.item39.invocationTargetException;

public class InvocationTarget {

    public int divideByZero() {
        return 1 / 0; // ArithmeticException 발생
    }
}
