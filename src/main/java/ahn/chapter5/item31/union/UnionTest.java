package ahn.chapter5.item31.union;

import org.junit.jupiter.api.Test;

import java.util.Set;

public class UnionTest {

    @Test
    void test() {
        // given
        Set<Integer> integers = Set.of(1, 2);
        Set<Double> doubles = Set.of(3.0, 4.0, 5.0);

        // when
        Set<Number> unionSet = Union.union(integers, doubles);
    }
}
