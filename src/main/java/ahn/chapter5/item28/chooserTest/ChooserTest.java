package ahn.chapter5.item28.chooserTest;

import org.junit.jupiter.api.Test;

import java.util.List;

public class ChooserTest {

//    public class Chooser {
//        private final Object[] choiceArray;
//
//        public Chooser(Collection choices) {
//            choiceArray = choices.toArray();
//        }
//
//        public Object choose() {
//            Random rnd = ThreadLocalRandom.current();
//            return choiceArray[rnd.nextInt(choiceArray.length)];
//        }
//    }

    @Test
    void test() {
        Object[] objects = new Object[2];
        objects[0] = "true";
        objects[1] = false;

        Chooser chooser = new Chooser(List.of(objects));

        while (true)
            System.out.println((String) chooser.choose());
    }
}
