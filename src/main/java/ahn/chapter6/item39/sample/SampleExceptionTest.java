package ahn.chapter6.item39.sample;

import java.util.ArrayList;
import java.util.List;

public class SampleExceptionTest {

    @ExceptionTest(ArithmeticException.class)
    public static void ma() { // 성공해야 한다.
        int i = 0;
        i = i / i;
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m2() { // 실패해야 한다. (다른 예외 발생)
        int[] a = new int[0];
        int i = a[1];
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m3() { // 실패해야 한다. (예외가 발생하지 않음)
    }

    @ExceptionTestContainer(value = {
            @ExceptionTest(IndexOutOfBoundsException.class),
            @ExceptionTest(NullPointerException.class)
    })
    public static void doublyBad() { // 성공해야 한다.
        List<String> list = new ArrayList<>();

        // 자바 API 명세에 따르면 다음 메서드는 IndexOutOfBoundsException이나 NullPointerException을 던질 수 있다.
        list.addAll(5, null);
    }
}
