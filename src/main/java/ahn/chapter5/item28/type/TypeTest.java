package ahn.chapter5.item28.type;

import org.junit.jupiter.api.Test;

public class TypeTest {

    @Test
    public void runtimeFailTest() {
        Object[] objectArray = new Long[1];
        objectArray[0] = "타입이 달라 넣을 수 없다."; // ArrayStoreException을 던진다.
    }

//    @Test
//    public void compileFailTest() {
//        List<Object> ol = new ArrayList<Long>(); // 호환되지 않는 타입이다.
//        ol.add("타입이 달라 넣을 수 없다.");
//    }
}
