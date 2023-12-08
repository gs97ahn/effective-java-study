package ahn.chapter5.item30.union;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class UnionTest {

    public static Set union1(Set s1, Set s2) {
        Set result = new HashSet(s1);
        result.addAll(s2);
        return result;
    }

    public static <E> Set<E> union2(Set<E> s1, Set<E> s2) {
        Set<E> result = new HashSet<>(s1);
        result.addAll(s2);
        return result;
    }

    @Test
    void test() {
        // given
        Set<String> guys = Set.of("톰", "딕", "해리");
        Set<String> stooges = Set.of("래리", "모에", "컬리");

        // when & then
        Set<String> aflCio = union2(guys, stooges);
        System.out.println(aflCio);
    }
}
