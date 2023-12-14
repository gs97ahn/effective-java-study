package ahn.chapter5.item31.chooser;

import org.junit.jupiter.api.Test;

import java.util.List;

public class ChooserTest {

    @Test
    void test() {
        // given
        List<Integer> list = List.of(1, 2, 3);

        // when
        Chooser<Number> chooser = new Chooser<>(list);
    }
}
