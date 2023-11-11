package ahn.chapter5.item26.stampCoin;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class StampCoinTest {

    class Stamp {
        private String s;

        public Stamp(String s) {
            this.s = s;
        }

        public void cancel() {}
    }

    class Coin {
        private String s;

        public Coin(String s) {
            this.s = s;
        }
    }

    @Test
    void positiveTest() {
        // given
        final Collection stamps = new ArrayList();

        // when
        stamps.add(new Coin("coin"));
        stamps.add(new Stamp("stamp"));
    }

    @Test
    void negativeTest() {
        // given
        final Collection stamps = new ArrayList();
        stamps.add(new Coin("coin"));
        stamps.add(new Stamp("stamp"));

        // when
        for (Iterator i = stamps.iterator(); i.hasNext(); ) {
            Stamp stamp = (Stamp) i.next(); // ClassCastException 을 던진다.
            stamp.cancel();
        }
    }

    @Test
    void negativeCompileTest() {
        // given
        final Collection<Stamp> stamps = new ArrayList<>();

        // when
        stamps.add(new Stamp("stamp"));
//        stamps.add(new Coin("coin"));
    }
}
