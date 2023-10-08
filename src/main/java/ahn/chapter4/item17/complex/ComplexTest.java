package ahn.chapter4.item17.complex;

import org.junit.jupiter.api.Test;

public class ComplexTest {

    @Test
    void 더하기() {
        ComplexFP cfp1 = new ComplexFP(1, 0);
        ComplexFP cfp2 = new ComplexFP(0, 1);

        ComplexPP cpp1 = new ComplexPP(1, 0);
        ComplexPP cpp2 = new ComplexPP(0, 1);

        System.out.println("값");
        System.out.print("함수형: " + cfp1.print() + " + " + cfp2.print() + " = ");

        ComplexFP cfpResult = cfp1.plus(cfp2); // 함수형 더하기

        System.out.println(cfpResult.print());

        System.out.print("절차적 혹은 명령형: " + cpp1.print() + " + " + cpp2.print() + " = ");

        ComplexPP cppResult = cpp1.add(cpp2); // 절차적 혹은 명령형 더하기

        System.out.println(cppResult.print());

        System.out.println("주소");
        System.out.println("함수형: " + cfp1 + " + " + cfp2 + " = " + cfpResult);
        System.out.println("절차적 혹은 명령형: " + cpp1 + " + " + cpp2 + " = " + cppResult);
    }

    @Test
    void 빼기() {
        ComplexFP cfp1 = new ComplexFP(1, 0);
        ComplexFP cfp2 = new ComplexFP(0, 1);

        ComplexPP cpp1 = new ComplexPP(1, 0);
        ComplexPP cpp2 = new ComplexPP(0, 1);

        System.out.println("값");
        System.out.print("함수형: " + cfp1.print() + " - " + cfp2.print() + " = ");

        ComplexFP cfpResult = cfp1.minus(cfp2); // 함수형 빼기

        System.out.println(cfpResult.print());

        System.out.print("절차적 혹은 명령형: " + cpp1.print() + " - " + cpp2.print() + " = ");

        ComplexPP cppResult = cpp1.subtract(cpp2); // 절차적 혹은 명령형 빼기

        System.out.println(cppResult.print());

        System.out.println("주소");
        System.out.println("함수형: " + cfp1 + " - " + cfp2 + " = " + cfpResult);
        System.out.println("절차적 혹은 명령형: " + cpp1 + " - " + cpp2 + " = " + cppResult);
    }
}
