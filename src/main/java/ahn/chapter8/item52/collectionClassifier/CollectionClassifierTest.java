package ahn.chapter8.item52.collectionClassifier;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class CollectionClassifierTest {

    @Test
    void test() {
        // given
        Collection<?>[] collections = {
                new HashSet<String>(),
                new ArrayList<BigInteger>(),
                new HashMap<String, String>().values()
        };

        // when & then
        for (Collection<?> c : collections) {
            System.out.println(CollectionClassifier.classify(c));
        }
    }
}
