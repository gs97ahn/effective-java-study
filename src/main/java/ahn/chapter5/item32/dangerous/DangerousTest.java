package ahn.chapter5.item32.dangerous;

import org.junit.jupiter.api.Test;

import java.util.*;

public class DangerousTest {

    @Test
    void dangerousTest() {
        // given
        List<String> stringList1 = new ArrayList<>(List.of("A", "B"));
        List<String> stringList2 = new ArrayList<>(List.of("C", "D"));

        // when
        Dangerous.dangerous(stringList1, stringList2);
    }

    @Test
    void pickTwoTest() {
        // given
        List<Coordinate> list1 = new ArrayList<>(List.of(new Coordinate(1, 1)));
        List<Coordinate> list2 = new ArrayList<>(List.of(new Coordinate(2, 2)));
        List<Coordinate> list3 = new ArrayList<>(List.of(new Coordinate(3, 3)));

        // when
        List<Coordinate>[] twoLists = Dangerous.pickTwo(list1, list2, list3);
    }
}
