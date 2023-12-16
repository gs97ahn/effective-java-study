package ahn.chapter5.item32.classCastException;

import org.junit.jupiter.api.Test;

public class ClassCastExceptionTest {

    @Test
    void classCastExceptionTestTest() {
        // given
        Object object = 1;
        String string = new String("0000");

        // when
        string = (String) object;
    }
}
