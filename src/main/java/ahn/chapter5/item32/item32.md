# 제네릭과 가변인수를 함께 쓸 때는 신중하라

## 가변인수 메서드와 제네릭

자바 5 때 가변인수(varargs) 메서드와 제네릭이 함께 추가되었습니다. 이 둘은 잘 어우러질 것이라 기대했지만 결과적으로 그렇지 않았습니다.

### 잘 어울리지 못한 이유

가변인수는 클라이언트가 메서드에 넘기는 인수의 수를 정할 수 있게합니다. 가변인수 메서드 호출시 가변인수를 담기 위한 배열이 자동으로 만들어지게 됩니다. 이 부분은 
내부적으로 감춰져서 동작을 해야되지만 클라이언트에 노출되는 문제가 발생하였습니다. 결과적으로 varargs 매개변수에 제네릭이나 매개 변수화 타입이 포함되어 컴파일되면 
아래와 같은 경고가 발생합니다.

```
warning: [unchecked] Possible heap pollution from parameterized vararg type List<String>
```

경고 발생 원인은 가변인수 메서드를 호출할 때 varargs 매개변수가 실체화 불가 타입으로 추론되기 때문입니다. 실체화 불가 타입은 컴파일타임에 타입을 대부분 추론하고 
런타임에는 타입 정보를 적게 가지고 있습니다. 그리고 거의 모든 제네릭과 매개변수화 타입은 실체화되지 않습니다. 결과적으로 매개변수화 타입의 변수가 다른 타입의 객체를
참조하면 힙 오염을 일으킬 수 있으며, 타입 안전성이 약해집니다.

##### 제네릭과 varargs를 혼용하면 타입 안정성이 깨진다!

```java
public class Dangerous {  
  
    static void dangerous(List<String>... stringLists ) {  
        List<Integer> intList = List.of(42);  
        Object[] objects = stringLists;  
        objects[0] = intList; // 힙 오염 발생  
        String s = stringLists[0].get(0); // ClassCastException  
    }  
}
```

```java
public class DangerousTest {  
  
    @Test  
    void dangerousTest() {  
        // given
		List<String> stringList1 = new ArrayList<>(List.of("A", "B"));
		List<String> stringList2 = new ArrayList<>(List.of("C", "D"));
		  
		// when
		Dangerous.dangerous(stringList1, stringList2);
    }
}
```

```
java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')
```

`dangerous()`에 형변환하는 곳이 보이지 않는데도 인수를 건네 호출하면 `ClassCastException`을 발생시킵니다. 이는 마지막줄인 
`String s = stringsLists[0].get(0)`에서 컴파일러가 생성한 형변환이 숨어 있기 때문입니다.

결과적으로 타입 안정성이 깨지기 때문에 제네릭 varargs 배열 매개변수에 값을 저장하는 것은 안전하지 않습니다.

```java
// 프로그래머가 제네릭 배열을 직접 생성
List<String>[] stringLists = new ArrayList<String>[1];

// 제네릭 varargs 매개변수를 받는 메서드
static void dangerous(List<String>... stringLists)
```

프로그래머가 제네릭 배열을 직접 생성하는 건 허용이 되지 않으며 컴파일타임에 오류를 발생시킵니다. 반면 제네릭 varargs 매개변수를 받는 메서드는 경고만 발생시키며 
컴파일이 됩니다. 그 이유는 제네릭이나 매개변수화 타입의 varargs 매개변수를 받는 메서드가 실무에서 매우 유용하기 때문에 언어 설계자는 이 모순을 수용하기로 결정하였습니다.

## 대표적인 제네릭이나 매개변수화 타입의 varargs 매개변수를 받는 메서드들

아래의 예시들은 전 예시들과 다르게 타입 안전성을 갖추고 있습니다. 그렇기 때문에 `@SuppressWarnings`나 `@SafeVarargs`를 활용하여 경고를 숨겨주고 
있습니다.

안정성을 확실하게 갖춘 이유는 메서드들이 순수하게 전달하는 일만하기 때문입니다. 이는 가변인수 메서드 호출시 varargs 매개변수를 담는 제네릭 배열이 생성되는데, 이 
배열에 아무것도 저장되지 않고 참조도 노출되지 않기에 타입 안정성을 갖추고 있습니다.

##### `Arrays.asList()`

```java
public class Arrays {
	// ...
	@SafeVarargs  
	@SuppressWarnings("varargs")  
	public static <T> List<T> asList(T... a) {  
	    return new ArrayList<>(a);  
	}
	// ...
}
```

##### `Collections.addAll()`

```java
public class Collections {
	// ...
	@SafeVarargs  
	public static <T> boolean addAll(Collection<? super T> c, T... elements) {  
	    boolean result = false;  
	    for (T element : elements)  
	        result |= c.add(element);  
	    return result;  
	}
	// ...
}
```

##### `EnumSet.of()`

```java
@SuppressWarnings("serial") // No serialVersionUID declared  
public abstract class EnumSet<E extends Enum<E>> extends AbstractSet<E>  
    implements Cloneable, java.io.Serializable  
{
	// ...
	@SafeVarargs  
	public static <E extends Enum<E>> EnumSet<E> of(E first, E... rest) {  
	    EnumSet<E> result = noneOf(first.getDeclaringClass());  
	    result.add(first);  
	    for (E e : rest)  
	        result.add(e);  
	    return result;
	}
	// ...
}
```

## 안전하지 않은 varargs 매개변수 배열

varargs 배열에 아무것도 저장하지 않고도 타입 안정성을 깰 수 있습니다.

```java
public class Coordinate {  
    private int x;  
    private int y;  
  
    public Coordinate(int x, int y) {  
        this.x = x;  
        this.y = y;  
    }  
}
```

```java
public class Dangerous {
  
    static <T> T[] toArray(T... args) {  
        return args;  
    }  
  
    static <T> T[] pickTwo(T a, T b, T c) {  
        switch (ThreadLocalRandom.current().nextInt(3)) {  
            case 0: return toArray(a, b);  
            case 1: return toArray(a, c);  
            case 2: return toArray(b, c);  
        }  
        throw new AssertionError(); // 도달할 수 없다.  
    }  
}
```

```java
public class DangerousTest {  
  
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
```

```
java.lang.ClassCastException: class [Ljava.lang.Object; cannot be cast to class [Ljava.util.List; ([Ljava.lang.Object; and [Ljava.util.List; are in module java.base of loader 'bootstrap')
```

`toArray()` 메서드가 반환하는 배열의 타입은 컴파일타임에 결정됩니다. 하지만 메서드가 호출되는 시점에 컴파일러에게 충분한 정보가 주어지지 않아 타입을 잘못 판단할
수 있습니다. 따라서 자신의 varargs 매개변수 배열을 그대로 반환하면 힙 오염을 이 메서드를 호출한 쪽의 콜스택으로까지 전이하는 결과가 발생합니다.

구체적으로 `Dangerous.pickTwo()`를 호출하면 `Object[]`를 반환하게 되는데 `Object[]`는 `List<Coordinate>[]`의 하위 타입이 아니기 때문에 
`ClassCastException`이 발생합니다.

### 힙 오염

힙 오염은 컴파일 타임에 Unchecked 경고가 일어나면 발생합니다. 그리고 런타임에서 타입이 선언된 변수가 다른 타입의 객체를 참조하게 되면 힙 오염이 발생할 수 있고 
`ClassCastException`으로 이어질 수 있습니다.

### `ClassCastException`

일반적으로 `ClassCastException`은 클래스의 형변환이 실패하게 되면 발생합니다.

그럼 메모리상 같은 용량을 차지하게 되면 `ClassCastException`을 던질까요?

```java
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
```

```
java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')
```

값이 1인 `Integer`를  `Object`로 인해 래퍼 클래스로 감싼 후 다시 `String`으로 형변환을 하고 시도하는 코드입니다.

이론상 `Integer`는 4 바이트고 `new String("0000")`로 인해 선언을 했기에 4 바이트를 가지고 있습니다. 하지만 여전히 `ClassCastExcetpion`이 
발생하고, 이는 JVM이 `Integer`와 `String`은 서로 인터페이스나 상속에 의한 관계가 없는 것을 인지함으로써 오류를 던진다는 것을 알 수 있습니다.

결과적으로 메모리상 같은 용량을 차지하게 되는 것과 `ClassCastException`은 아예 상관이 없게 됩니다.

> C 언어 였다면 "0000"은 사실 null을 포함해 "0000\\0"로 5 바이트겠지만 Java 에서는 내부적으로 `String`의 끝을 추적하기에 null이 문장 끝에 필요 
> 없습니다. 그래서 "0000"은 Java에서 4 바이트입니다.

## 제네릭 varargs 매개변수 배열에 다른 메서드가 접근 가능한 예시

일반적으로 제네릭 varargs 매개변수 배열에 다른 메서드가 접근하도록 허용하면 안전하지 않습니다. 하지만 이에 대한 예외가 있습니다.

1. `@SafeVarargs`로 제대로 애노테이트된 또 다른 varargs 메서드에 넘기는 것은 안전합니다.
2. 배열 내용의 일부 함수를 호출만 하는 varargs를 받지 않는 일반 메서드에 넘기는 것은 안전합니다.

##### 제네릭 varargs 매개변수를 안전하게 사용하는 메서드

```java
public class Safe {  
  
    @SafeVarargs  
    static <T> List<T> flatten(List<? extends T>... lists) {  
        List<T> result = new ArrayList<>();  
        for (List<? extends T> list : lists)  
            result.addAll(list);  
        return result;  
    }  
}
```

```java
public class SafeTest {  
  
    @Test  
    void flattenTest() {  
        // given  
        List<String> strings1 = new ArrayList<>(List.of("A", "B"));  
        List<String> strings2 = new ArrayList<>(List.of("C", "D"));  
  
        // when  
        List<String> result = Safe.flatten(strings1, strings2);  
  
        System.out.println(result);  
    }  
}
```

```
[A, B, C, D]
```

### `@SafeVarargs` 애너테이션 사용 규칙

제네릭이나 매개변수화 타입의 varargs 매개변수를 받는 모든 메서드에 `@SafeVaraargs`를 달면 됩니다. 그리고 안전하지 않은 varargs 메서드는 절대 작성하면 
안됩니다.

### 안전한 제네릭 varargs 메서드

제네릭 varargs 메서드 작성시 아래의 두 조건을 모두 만족해야 되며, 그렇지 않을 경우 무조건 수정을 해야됩니다.

1. varargs 매개변수 배열에 아무것도 저장하지 않습니다.
2. 그 배열 혹은 복제본을 신뢰할 수 없는 코드에 노출하지 않습니다.

`@SafeVarargs` 애너테이션은 자바 8에서 정적 메서드와 final 인스턴스에만 붙이는 것을 허용했고 자바 9부터는 private 인스턴스 메서드에도 허용하였습니다.

## `@SafeVarargs` 애너테이션 외 다른 방법

`@SafeVarargs` 애너테이션이 유일한 정답은 아닙니다. `@SafeVarargs` 애너테이션 활용 외의 List를 활용한 두 가지 방법이 있습니다.

1. varargs 매개변수를 List 매개변수로 대체합니다.
2. 기존의 자바 라이브러리르 활용하여 직접 메서드를 작성합니다.

장점
- 컴파일러가 메서드의 타입 안전성을 검증하기에 클라이언트가 실수로 안전하다고 잘못 판단할 가능성이 없습니다.

단점
- 클라이언트 코드가 약간 지저분해집니다.
- 코드의 속도가 약간 느려집니다.

##### 1. 제네릭 varargs 매개변수를 List로 대체한 예 - 타입 안전하다.

```java
public class Safe {

	static <T> List<T> flatten(List<List<? extends T>> lists) {
		List<T> result = new ArrayList<>();
        for (List<? extends T> list : lists)
            result.addAll(list);
        return result;
    }
}
```

```java
public class SafeTest {
  
    @Test  
    void flattenTest() {  
        // given  
        List<String> strings1 = new ArrayList<>(List.of("A", "B"));  
        List<String> strings2 = new ArrayList<>(List.of("C", "D"));  
  
        // when  
        List<String> result = Safe.flatten(List.of(strings1, strings2));  
  
        System.out.println(result);  
    }  
}
```

```
[A, B, C, D]
```

##### 2. 기존의 자바 라이브러리를 활용하여 직접 메서드를 작성한 예 - 타입 안전한다.

```java
public class Safe {

	// <T> T[] -> <T> List<T>
    static <T> List<T> pickTwo(T a, T b, T c) {  
        switch (ThreadLocalRandom.current().nextInt(3)) {  
	        // toArray(a, b) -> List.of(a, b)
            case 0: return List.of(a, b);
            case 1: return List.of(a, c);
            case 2: return List.of(b, c);
        }  
        throw new AssertionError(); // 도달할 수 없다.
    }  
}
```

```java
public class SafeTest {  
  
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
```

```
[A, B]
[C, D]
```

## 정리

가변인수와 제네릭은 잘 어우러지지 않습니다. 가변인수는 배열을 노출하기에 완벽한 추상화가 되지 않았습니다. 제네릭 varargs 매개변수는 타입 안전성을 갖추지 
못하였지만 이를 허용한 이유는 실무에서의 유용성 때문입니다.

List를 활용하면 varargs 매개변수 사용을 회피할 수 있습니다. 하지만 반드시 varargs 매개변수를 써야 된다면 타입 안전성을 확실히 확인한 뒤 
`@SafeVarargs`를 달아야 됩니다.

### 참고

- [https://docs.oracle.com/javase/tutorial/java/generics/nonReifiableVarargsType.html#heap_pollution](https://docs.oracle.com/javase/tutorial/java/generics/nonReifiableVarargsType.html#heap_pollution)
