package ahn.chapter6.item34.operation;

import org.junit.jupiter.api.Test;

public class OperationTest {

    @Test
    void toStringTest() {
        // given
        double x = 2.0;
        double y = 4.0;

        // when & then
        for (Operation op : Operation.values())
            System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x, y));
    }

    @Test
    void fromStringTest() {
        // given
        String symbol = "+";

        // when
        Operation operation = Operation.fromString(symbol)
                .orElseThrow(() -> {
                    throw new AssertionError("알 수 없는 연산");
                });

        // then
        System.out.println(operation);
    }
}
