package ahn.chapter6.item34.planet;

import org.junit.jupiter.api.Test;

public class PlanetTest {

    @Test
    void test() {
        double earthWeight = 80.0;
        double mass = earthWeight / Planet.EARTH.surfaceGravity();
        for (Planet p : Planet.values())
            System.out.printf("%s에서의 무게는 %f이다.%n", p, p.surfaceWeight((mass)));
    }
}
