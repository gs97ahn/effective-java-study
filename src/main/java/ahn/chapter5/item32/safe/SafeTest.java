package ahn.chapter5.item32.safe;

import ahn.chapter5.item32.dangerous.Coordinate;
import ahn.chapter5.item32.dangerous.Dangerous;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SafeTest {

    @Test
    void flattenTest1() {
        // given
        List<String> strings1 = new ArrayList<>(List.of("A", "B"));
        List<String> strings2 = new ArrayList<>(List.of("C", "D"));

        // when
        List<String> result = Safe.flatten(strings1, strings2);

        System.out.println(result);
    }

    @Test
    void flattenTest2() {
        // given
        List<String> strings1 = new ArrayList<>(List.of("A", "B"));
        List<String> strings2 = new ArrayList<>(List.of("C", "D"));

        // when
        List<String> result = Safe.flatten(List.of(strings1, strings2));

        System.out.println(result);
    }

    @Test
    void pickTwoTest() {
        // given
        List<String> list1 = new ArrayList<>(List.of("A", "B"));
        List<String> list2 = new ArrayList<>(List.of("C", "D"));
        List<String> list3 = new ArrayList<>(List.of("E", "F"));

        // when
        List<List<String>> twoLists = Safe.pickTwo(list1, list2, list3);

        twoLists.forEach(System.out::println);
    }
}
