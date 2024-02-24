# 명명 패턴보다 애너테이션을 사용하라

전통적으로 도구나 프레임워크가 특별히 다뤄야 할 프로그램 요소에는 구분되는 명명 패턴을 적용해 왔다. 하지만 명명 패턴은 세 가지 단점이 존재한다.

첫 번째 단점은 오타가 나면 안된다는 것이다. JUnit은 버전 3까지 테스트 메서드명이 `test`로 시작하끔 했는데, `tsetSafety` 같이 오타가 발생하면 JUnit이 해당 테스트 메서드를 수행하지 않는다.

두 번째 단점은 올바른 프로그램 요소에서만 사용되리라 보증할 수 없다는 것이다. 예를 들어, 클래스 내에 테스트 메서드들이 수행되기를 기대하며 `TestSafety`라는 클래스 만든다. 하지만 테스트 JUnit은 클래스 이름에는 관심이 없기 때문에 테스트가 수행되지 않으며 경고 메세지 조차 출력되지 않을 것이다.

세 번째 단점은 프로그램 요소를 매개변수로 전달할 방법이 없는 것이다. 테스트에서 특정 예외가 발생해야 성공하는 테스트가 있고, 해당 예외 타입을 매개변수로 전달해야 된다고 예를 들겠다. 하지만 이러한 예시를 수행할 수 있는 방법이 마땅히 없다.

## JUnit에서 애너테이션 활용

위의 문제들을 해결하기 위해 JUnit 버전 4부터 애너테이션을 활용하였다.

##### 마커(marker) 애너테이션 타입 선언

```java
import java.lang.annotation.*;

/**
 * 테스트 메서드임을 선언하는 애너테이션이다.
 * 매개변수 없는 정적 메서드 전용이다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {}
```

## 메타애너테이션

메타애너테이션(meta-annotation)은 애너테이션 선언에 다는 애너테이션이다. `Test` 위에 있는 `@Retention`과 `@Target`을 확인할 수 있다.

### `@Retention`

`@Retention`은 애노테이션의 생명 주기를 정의한다. `@Retention`은 세 가지의 생명 주기 정책이 있다.

- `RetentionPolicy.SOURCE`: 런타임에서 무시되며 사실상 주석 같은 역할을 한다.
- `RetentionPolicy.CLASS`: `.class` 파일 생성까지 유지되지만 컴파일러가 이후에는 무시한다. 그리고 `RetentionPolciy`에 기본 값이다.
- `RetentionPolicy.RUNTIME`: 런타임까지 사용될 수 있으며 종료될 때까지 메모리에서 살아있다.

### `@Target`

`@Target`은 해당 애너테이션이 어느 위치에 부착될 수 있을지 지정하는 애너테이션이다. 위에 `@Test` 경우 `ElementType.METHOD`이기 때문에 메서드 레벨에 부착할 수 있다고 나와 있다. `@Target`은 아홉 가지의 타입이 있다.

- `ElementType.TYPE`: 클래스, 인터페이스, Enum
- `ElementType.FIELD`: 필드
- `ElementType.METHOD`: 메서드
- `ElementType.CONSTRUCTOR`: 생성자
- `ElementType.LOCAL_VARIABLE`: 지역 변수
- `ElementType.ANNOTATION_TYPE`: 애너테이션 타입
- `ElementType.PACKAGE`: 패키지
- `ElementType.TYPE_PARAMETER`: 타입 매개변수
- `ElementType.PARAMETER`: 매개변수

### 애너테이션 사용시 주의 사항

컴파일러는 위의 제약을 강제하지 않기 때문에 적절한 애너테이션을 잘 사용해야 된다. 물론 위의 애너테이션 없이 실행을 한다면 문제가 발생하지 않는다. 하지만 테스트 도구를 실행할 때 문제가 된다.

##### 마커 애너테이션을 사용한 프로그램 예

```java
public class SampleTest {
	
	@Test
	public static void m1() { } // 성공해야 한다.

	public static void m2() { }

	@Test
	public static void m3() { // 실패해야 한다.
		throw new RuntimeException("실패");
	}

	public static void m4() { }

	@Test
	public void m5() { } // 잘못 사용한 예: 정적 메서드가 아니다.

	public static void m6() { }

	@Test
	public static void m7() {
		throw new RuntimeException("실패");
	}

	public static void m8() { }
}
```

우선 정적 메서드가 아닌 테스트 메서드들은 실행되지 않는다. 7개의 정적 메서드가 있지만 그 중 3개만 `@Test`가 달려 있어서 테스트 도구로 인해 실행 된다. 그래서 해당 프로그램을 실행하면 2개는 성공, 1개는 실패로 나온다.

## 마커 애너테이션

`@Test`는 클래스의 의미에 직접적인 영향을 주지 않는다. 애너테이션에 관심 있는 프로그래머에 추가 정보를 제공만 한다. 아래의 `RunTests`가 그러한 도구의 예시다.

아래의 테스트 러너는 `isAnnotationPresent()` 메서드를 통해 `@Test` 애너테이션이 달린 메서드를 찾아 실행한다. 테스트가 실패하면 `InvocationTarggetException`을 감싸서 `cause`, 즉 실패 정보를 출력한다. `InvocationTargetException` 외에 잘못된 인스턴스 메서드, 매개변수가 있는 메서드, 호출할 . 수없는 메서드 등의 예외는 두 번째 catch블록으로 잡는다.

##### 마커 애너테이션을 처리하는 프로그램

```java
import java.lang.reflect.*;

public class RunTests {  
  
    public static void main(String[] args) throws Exception {  
        int tests = 0;  
        int passed = 0;  
        Class<?> testClass = Class.forName(args[0]);  
        for (Method m : testClass.getDeclaredMethods()) {  
            if (m.isAnnotationPresent(Test.class)) {  
                tests++;  
                try {  
                    m.invoke(null);  
                    passed++;  
                } catch (InvocationTargetException wrappedExc) {  
                    Throwable exc = wrappedExc.getCause();  
                    System.out.println(m + "실패: " + exc);  
                } catch (Exception exc) {  
                    System.out.println("잘못 사용한 @Test: " + m);  
                }  
            }  
        }  
        System.out.printf("성공: %d, 실패: %d%n", passed, tests - passed);  
    }  
}
```

위에 m8까지 있는 테스를 수행하면 다음 메시지가 출력 된다.

```
public static void Sample.m3() failed: RuntimeException: Boom Invalid @Test: pbulic void Sample.m5()
public static void sample.m7() failed: RuntimeException: Crash
성공: 1, 실패: 3
```

### `InvocationTargetException`으로 예외를 감싸는 이유

```java
public class InvocationTargetException extends ReflectiveOperationException {

    private static final long serialVersionUID = 4085088731926701167L;
    private Throwable target;

    protected InvocationTargetException() {
        super((Throwable) null);
    }
}
```

위에서 언급 했듯이 테스트를 수행할 때 에외가 발생하면 해당 예외를 바로 던지지 않고 `InvocationTargetException`을 감싸서 던진다. 그 이유는 [테스트가 리플렉션 레이어를 통해 실행되기 때문이다](https://www.baeldung.com/java-lang-reflect-invocationtargetexception). 그래서 실패 사유를 알기 위해서는 `InvocationTargetException`으로 감싸야 된다.

그래서 테스트 실행을 통해 예외를 분석하고 싶다면, 다음과 같이 코드를 작성해야 된다.

```java
public class InvocationTarget {  
  
    public int divideByZero() {  
        return 1 / 0; // ArithmeticException 발생  
    }  
}
```

```java
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
```

## 특정 예외를 던져야 성공하는 테스트

##### 매개변수 하나를 받는 애너테이션 타입

```java
import java.lang.annotation.*;

/**
 * 명시한 예외가 던져야만 성공하는 테스트 메서드용 애너테이션
 */
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.METHOD)
 public @interface ExceptionTest {
	 Class<? extends Throwable> value();
 }
```

`ExceptionTest`는 `Class<? extends Throwable>`, 즉 와일드카드 타입을 통해 모든 예외를 수용할 수 있다.

##### 매개변수 하나짜리 애너테이션을 사용한 프로그램

```java
public class SampleExceptionTest {  
  
    @ExceptionTest(ArithmeticException.class)  
    public static void ma() { // 성공해야 한다.  
        int i = 0;  
        i = i / i;  
    }  
  
    @ExceptionTest(ArithmeticException.class)  
    public static void m2() { // 실패해야 한다. (다른 예외 발생)  
        int[] a = new int[0];  
        int i = a[1];  
    }  
  
    @ExceptionTest(ArithmeticException.class)  
    public static void m3() { // 실패해야 한다. (예외가 발생하지 않음)  
    }  
}
```

```java
public class RunExceptionTest {  
  
    public static void main(String[] args) throws Exception {  
        int tests = 0;  
        int passed = 0;  
        Class<?> testClass = Class.forName(args[0]);  
        for (Method m : testClass.getDeclaredMethods()) {  
            if (m.isAnnotationPresent(ExceptionTest.class)) {  
                tests++;  
                try {  
                    m.invoke(null);  
                    System.out.printf("테스트 %s 실패: 예외를 던지지 않음%n", m);  
                } catch (InvocationTargetException wrappedExc) {  
                    Throwable exc = wrappedExc.getCause();  
                    Class<? extends Throwable> excType = m.getAnnotation(ExceptionTest.class).value();  
                    if (excType.isInstance(exc)) {  
                        passed++;  
                    } else {  
                        System.out.printf("테스트 %s 실패: 기대한 예외 %s, 발생한 예외 %s%n", m, excType.getName(), exc);  
                    }  
                } catch (Exception exc) {  
                    System.out.println("잘못 사용한 @ExceptionTest: " + m);  
                }  
            }  
        }  
    }  
}
```

이전에 `SampleTest`,  `RunTest`와 비슷한 형태이지만 특정 예외가 던져지는 검증하는 코드가 추가되었다. 그리고 형변환도 거지치 않기 때문에 `ClassCastException`도 걱정할 필요 없다.

## 여러 예외를 던져야 성공하는 테스트

##### 배열 매개변수를 받는 애너테이션 타입

배열을 통해 이전에 만든 `ExceptionTest`를 수정 없이 모두 수용하면서 두 개 이상의 예외가 던져지는 검증할 수 있다.

```java
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.METHOD)
public @interface ExceptionTest {  
    Class<? extends Throwable>[] value();  
}
```

##### 배열 매개변수를 받는 애너테이션을 사용하는 코드

```java
public class SampleExceptionTest {  

    @ExceptionTest({ IndexOutOfBoundsException.class,   
        NullPointerException.class })
    public static void doublyBad() { // 성공해야 한다.  
        List<String> list = new ArrayList<>();  
  
        // 자바 API 명세에 따르면 다음 메서드는 IndexOutOfBoundsException이나 NullPointerException을 던질 수 있다.  
        list.addAll(5, null);  
    }  
}
```

```java
public class RunExceptionTest {  
  
    public static void main(String[] args) throws Exception {  
        int tests = 0;  
        int passed = 0;  
        Class<?> testClass = Class.forName(args[0]);  
        for (Method m : testClass.getDeclaredMethods()) {  
            if (m.isAnnotationPresent(ExceptionTest.class)) {  
                tests++;  
                try {  
                    m.invoke(null);  
                    System.out.printf("테스트 %s 실패: 예외를 던지지 않음%n", m);  
                } catch (Throwable wrappedExc) {  
                    Throwable exc = wrappedExc.getCause();  
                    int oldPassed = passed;  
                    Class<? extends Throwable>[] excTypes = m.getAnnotation(ExceptionTest.class).value();  
                    for (Class<? extends Throwable> excType : excTypes) {  
                        if (excType.isInstance(exc)) {  
                            passed++;  
                            break;  
                        }  
                    }  
                    if (passed == oldPassed)  
                        System.out.printf("테스트 %s 실패: %s %n", m, exc);  
                }  
            }  
        }  
    }  
}
```

### 다른 방식으로 여러 값을 받는 애너테이션 만드는 법

배열 매개변수를 사용하지 않고 `@Repeatable` 애너테이션을 활용하면 동일하게 동작하는 코드를 작성할 수 있다. 하지만 세 가지 주의 사항이 있다.

#### `@Repeatable` 애너테이션 활용시 주의사항

1.  `@Repeatable` 애너테이션은 반환하는 컨테이너 애너테이션을 정의하고 `Repeatable`에 이 컨테이너 애너테이션의 class 객체를 매개변수로 전달해야 한다.
2.  컨테이너 애너테이션은 내부 애너테이션 타입의 배열을 반환하는 value 메서드를 정의해야 한다.
3. 컨테이너 애너테이션 타입에는 적절한 보존 정책인 `@Retention`과 적용 대상 `@Target`을 명시해야 한다.

##### 반복 가능한 애너테이션 타입

```java
// 반복 가능한 애너테이션
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ExceptionTestContainer.class)
public @interface ExceptionTest {
	Class<? extends Throwable> value();
}
```

```java
// 컨테이너 애너테이션
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.MEHTOD)
public @interface ExceptionTestContainer {
	ExceptionTest[] value();
}
```

```java
public class RunExceptionTest {  
  
    public static void main(String[] args) throws Exception {  
        int tests = 0;  
        int passed = 0;  
        Class<?> testClass = Class.forName(args[0]);  
        for (Method m : testClass.getDeclaredMethods()) {  
            if (m.isAnnotationPresent(ExceptionTest.class)  
                    || m.isAnnotationPresent(ExceptionTestContainer.class)) {  
                tests++;  
                try {  
                    m.invoke(null);  
                    System.out.printf("테스트 %s 실패: 예외를 던지지 않음 %n", m);  
                } catch (Throwable wrappedExc) {  
                    Throwable exc = wrappedExc.getCause();  
                    int oldPassed = passed;  
                    ExceptionTest[] excTests = m.getAnnotationsByType(ExceptionTest.class);  
                    for (ExceptionTest excTest : excTests) {  
                        if (excTest.value().isInstance(exc)) {  
                            passed++;  
                            break;  
                        }  
                    }  
                    if (passed == oldPassed)  
                        System.out.printf("테스트 %s 실패: %s %n", m, exc);  
                }  
            }  
        }  
    }  
}
```

```java
public class SampleExceptionTest {
	
    @ExceptionTestContainer(value = {  
            @ExceptionTest(IndexOutOfBoundsException.class),  
            @ExceptionTest(NullPointerException.class)  
    })  
    public static void doublyBad() { // 성공해야 한다.  
        List<String> list = new ArrayList<>();  
  
        // 자바 API 명세에 따르면 다음 메서드는 IndexOutOfBoundsException이나 NullPointerException을 던질 수 있다.  
        list.addAll(5, null);  
    }  
}
```



### 참고

- [https://www.geeksforgeeks.org/java-retention-annotations/](https://www.geeksforgeeks.org/java-retention-annotations/)
- [https://www.baeldung.com/java-lang-reflect-invocationtargetexception](https://www.baeldung.com/java-lang-reflect-invocationtargetexception)