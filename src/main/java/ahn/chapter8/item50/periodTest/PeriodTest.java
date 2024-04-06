package ahn.chapter8.item50.periodTest;

import org.junit.jupiter.api.Test;

import java.util.Date;

public class PeriodTest {

    @Test
    void constructorTest() {
        // given
        Date start = new Date();
        Date end = new Date();
        Period p = new Period(start, end);

        System.out.println("수정 전: " + p.end());

        // when
        end.setYear(78); // p의 내부를 수정했다.

        // then
        System.out.println("수정 후: " + p.end());
    }

    @Test
    void returnTest() {
        // given
        Date start = new Date();
        Date end = new Date();
        Period p = new Period(start, end);

        System.out.println("수정 전: " + p.end());

        // when
        p.end().setYear(78);

        // then
        System.out.println("수정 후: " + p.end());
    }
}
