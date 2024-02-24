package ahn.chapter6.item39.invocationTargetException;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InvocationTargetExceptionTest {

    @Test
    void test() throws NoSuchMethodException {
        InvocationTarget invocationTarget = new InvocationTarget();
        Method method = InvocationTarget.class.getMethod("divideByZero");

        Exception exception = assertThrows(InvocationTargetException.class,
                () -> method.invoke(invocationTarget));

        assertEquals(ArithmeticException.class, exception.getCause().getClass());
    }
}
