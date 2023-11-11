package ahn.chapter5.item26.wildCard;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class WildCardTest {

    @Test
    void rawTypeTest() {
        // given
        Set set = new HashSet();

        // when
        set.add("verboten");
        set.add(42);
    }

    @Test
    void positiveUnboundedWildCardTest() {
        // given
        Set<?> set = new HashSet<>();

        // when
        set.add(null);
    }

    @Test
    void negativeUnboundedWildCardTest() {
        // given
        Set<?> set = new HashSet<>();

        // when
//        set.add("verboten");
    }
}
