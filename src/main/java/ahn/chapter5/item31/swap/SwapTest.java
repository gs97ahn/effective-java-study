package ahn.chapter5.item31.swap;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SwapTest {

    @Test
    void test() {
        // given
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3));

        list.forEach(e -> System.out.print(e + " "));
        System.out.println();

        // when
        Swap.swap(list, 0, 2);

        // then
        list.forEach(e -> System.out.print(e + " "));
        System.out.println();
    }
}
