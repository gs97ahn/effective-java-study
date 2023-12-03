package ahn.chapter5.item30.maxTest;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class MaxTest {

    public static <E extends Comparable<E>> E max(Collection<E> c) {
        if (c.isEmpty())
            throw new IllegalArgumentException("컬렉션이 비어 있습니다.");

        E result = null;
        for (E e : c)
            if (result == null || e.compareTo(result) > 0)
                result = Objects.requireNonNull(e);

        return result;
    }

    @Test
    void stringTest() {
        // given
        List<String> strings = List.of("가", "나", "다");

        // when & then
        String max = max(strings);
        System.out.println(max);
    }

    @Test
    void intTest() {
        // given
        List<Integer> integers = List.of(1, 2, 3);

        // when & then
        int max = max(integers);
        System.out.println(max);
    }
}
