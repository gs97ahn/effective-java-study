package ahn.chapter5.item30.unaryOperatorTest;

import org.junit.jupiter.api.Test;

import java.util.function.UnaryOperator;

public class UnaryOperatorTest {

    @Test
    void test() {
        String[] strings = { "삼베", "대마", "나일론" };
        UnaryOperator<String> sameString = UnaryOperator.identity();
        for (String s : strings)
            System.out.println(sameString.apply(s));

        Number[] numbers = { 1, 2.0, 3L };
        UnaryOperator<Number> sameNumber = UnaryOperator.identity();
        for (Number n : numbers)
            System.out.println(sameNumber.apply(n));
    }
}
