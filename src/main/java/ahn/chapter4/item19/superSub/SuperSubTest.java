package ahn.chapter4.item19.superSub;

import org.junit.jupiter.api.Test;

public class SuperSubTest {

    @Test
    void test1() {
        Sub sub = new Sub();
        sub.overrideMe();
    }
}
