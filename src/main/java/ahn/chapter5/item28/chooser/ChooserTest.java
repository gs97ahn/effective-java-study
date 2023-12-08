package ahn.chapter5.item28.chooser;

import org.junit.jupiter.api.Test;

import java.util.List;

public class ChooserTest {
    @Test
    void test() {
        // given
        List<Object> list = List.of(new Box(1, 1), false);
        Chooser chooser = new Chooser(list);

        // when & then
        while (true) {
            Box box = (Box) chooser.choose();
            box.size();
        }
    }
}
