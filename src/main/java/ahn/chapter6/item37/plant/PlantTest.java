package ahn.chapter6.item37.plant;

import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class PlantTest {

    @Test
    void badExampleTest() {
        // given
        Plant[] garden = {
                new Plant("장미", Plant.LifeCycle.ANNUAL),
                new Plant("코스모스", Plant.LifeCycle.ANNUAL),
                new Plant("튤립", Plant.LifeCycle.PERENNIAL),
                new Plant("민들레", Plant.LifeCycle.BIENNIAL),
                new Plant("국화", Plant.LifeCycle.BIENNIAL)
        };

        // when
        Set<Plant>[] plantsByLifeCycle = (Set<Plant>[]) new Set[Plant.LifeCycle.values().length];
        for (int i = 0; i < plantsByLifeCycle.length; i++)
            plantsByLifeCycle[i] = new HashSet<>();

        for (Plant p : garden)
            plantsByLifeCycle[p.lifeCycle.ordinal()].add(p);

        // then
        for (int i = 0; i < plantsByLifeCycle.length; i++)
            System.out.printf("%s: %s%n", Plant.LifeCycle.values()[i], plantsByLifeCycle[i]);
    }

    @Test
    void goodExampleTest() {
        // given
        Plant[] garden = {
                new Plant("장미", Plant.LifeCycle.ANNUAL),
                new Plant("코스모스", Plant.LifeCycle.ANNUAL),
                new Plant("튤립", Plant.LifeCycle.PERENNIAL),
                new Plant("민들레", Plant.LifeCycle.BIENNIAL),
                new Plant("국화", Plant.LifeCycle.BIENNIAL)
        };

        // when
        Map<Plant.LifeCycle, Set<Plant>> plantsByLifeCycle = new EnumMap<>(Plant.LifeCycle.class);
        for (Plant.LifeCycle lc : Plant.LifeCycle.values())
            plantsByLifeCycle.put(lc, new HashSet<>());

        for (Plant p : garden)
            plantsByLifeCycle.get(p.lifeCycle).add(p);

        // then
        System.out.println(plantsByLifeCycle);
    }

    @Test
    void streamExampleTest1() {
        // given
        Plant[] garden = {
                new Plant("장미", Plant.LifeCycle.ANNUAL),
                new Plant("코스모스", Plant.LifeCycle.ANNUAL),
                new Plant("튤립", Plant.LifeCycle.PERENNIAL),
                new Plant("민들레", Plant.LifeCycle.BIENNIAL),
                new Plant("국화", Plant.LifeCycle.BIENNIAL)
        };

        // when & then
        System.out.println(Arrays.stream(garden)
                .collect(groupingBy(p -> p.lifeCycle)));
    }

    @Test
    void streamExampleTest2() {
        // given
        Plant[] garden = {
                new Plant("장미", Plant.LifeCycle.ANNUAL),
                new Plant("코스모스", Plant.LifeCycle.ANNUAL),
                new Plant("튤립", Plant.LifeCycle.PERENNIAL),
                new Plant("민들레", Plant.LifeCycle.BIENNIAL),
                new Plant("국화", Plant.LifeCycle.BIENNIAL)
        };

        // when & then
        System.out.println(Arrays.stream(garden)
                .collect(groupingBy(p -> p.lifeCycle,
                        () -> new EnumMap<>(Plant.LifeCycle.class), toSet())));
    }
}
