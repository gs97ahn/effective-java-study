package ahn.chapter4.item18.instrumented;

import org.junit.jupiter.api.Test;

import java.util.List;

public class InstrumentedHashSetTest {

    @Test
    void test1() {
        InstrumentedHashSet<String> s = new InstrumentedHashSet<>();
        s.addAll(List.of("틱", "탁탁", "펑"));

        System.out.println("기대값 = 3");
        System.out.println("실제값 = " + s.getAddCount());
    }
}
