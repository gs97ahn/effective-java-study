package ahn.chapter6.item40.bigram;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class BigramTest {

    @Test
    void test() {
        // given
        Set<Bigram> s = new HashSet<>();

        // when
        for (int i = 0; i < 10; i++)
            for (char ch = 'a'; ch <= 'z'; ch++)
                s.add(new Bigram(ch, ch));

        // then
        System.out.println(s.size());
    }
}
