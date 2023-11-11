package ahn.chapter5.item26.safety;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SafetyTest {

    @Test
    void positiveTest() {
        System.out.println("컴파일 완료");

        // given
        List<String> strings = new ArrayList<>();

        // when
        unsafeAdd(strings, Integer.valueOf(42));
    }

    @Test
    void negativeTest() {
        System.out.println("컴파일 완료");

        // given
        List<String> strings = new ArrayList<>();

        // when
        unsafeAdd(strings, Integer.valueOf(42));
        String s = strings.get(0); // 컴파일러가 자동으로 형변환 코드를 넣어준다.
    }

    @Test
    void compileNegativeTest() {
        System.out.println("컴파일 완료");

        // given
        List<String> strings = new ArrayList<>();

        // when
//        safeAdd(strings, Integer.valueOf(42));
//        String s = strings.get(0);
    }

    private void unsafeAdd(List list, Object o) {
        list.add(o);
    }

    private void safeAdd(List<Object> list, Object o) {
        list.add(o);
    }
}
