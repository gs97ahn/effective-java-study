# 이왕이면 제네릭 타입으로 만들라

## Object 기반 스택 - 제네릭이 절실한 강력 후보

```java
public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (size == 0)
            throw new EmptyStackException();
        Object result = elements[--size];
        elements[size] = null; // 다 쓴 참조 해제
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}
```

이 클래스는 원래 제네릭 타입이어야 됩니다. 이 클래스를 제네릭으로 바꾼다고 해도 현재 버전을 사용하는 클라이언트에는 아무 지장이 없습니다. 현상태의 클라이언트는 
스택에서 꺼낸 객체를 형변환해야 하는데, 이 때 런타임 오류가 날 위험이 있습니다.

## 제네릭 스택으로 가는 단계

먼저 클래스 선언에 타입 이름으로 `E`를 타입 매개 변수로 추가합니다.

```java
public class Stack<E> {
    private E[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new E[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public E pop() {
        if (size == 0)
            throw new EmptyStackException();
        E result = elements[--size];
        elements[size] = null; // 다 쓴 참조 해제
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}
```

##### 결과

```
error: generic array creation
        elements = new E[DEFAULT_INITIAL_CAPACITY];
                   ^
```

`E`와 같은 실체화 불가 타입으로는 배열을 만들 수 없습니다. 이를 해결하기 위한 두 가지 해결책이 있습니다.

## 두 가지 해결책
### 1. 제네릭 배열 생성을 금지하는 제약을 대놓고 우회하는 방법

```java
public class Stack<E> {
    // ...
    // 배열 elements는 push(E)로 넘어온 E 인스턴스만 담는다.
    // 따라서 타입 안전성을 보장하지만,
    // 이 배열의 런타임 타입은 E[]가 아닌 Object[]다!
    @SuppressWarnings("unchecked")
    public Stack() {
        elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
    }
    // ...
}
```

```
warning: [unchecked] unchecked cast
found: Object[], required: E[]
        elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
                        ^
```

`Object` 배열을 생성한 다음 제네릭 배열로 형변환하면 오류는 더이상 발생하지 않고 경고가 발생합니다. 이는 타입이 일반적으로 안전하지 않기 때문입니다. 프로그램이 
안전성을 확인할 수 없기 때문에 우리가 직접 확인을 해야합니다. 안전함을 직접 증명한 후 최소 범위로 좁혀 `@SuppressWarnings`로 경고를 숨겨야 됩니다.


### 2. `elements` 필드의 타입을 `E[]`에서 `Object[]`로 바꾸는 방법

```java
public class Stack<E> {
    // ...
    public E pop() {
        if (size == 0)
            throw new EmptyStackException();
        E result = elements[--size];
        elements[size] = null; // 다 쓴 참조 해제
        return result;
    }
    // ...
}
```

```
incompatible types
found: Object, required: E
        E result = elements[--size];
                            ^
```

컴파일 오류가 발생하는데 아래와 같이 배열이 반환한 원소를 `E`로 형변환하면 오류 대신 경고가 뜹니다. 이는 실체화 불가 타입인 `E` 때문인데, 컴파일러는 런타임에 
이뤄지는 형변환이 안전한지 증명할 방법이 없습니다.

```java
public class Stack<E> {
    // ...
    public E pop() {
        if (size == 0)
            throw new EmptyStackException();
        E result = (E) elements[--size];
        elements[size] = null; // 다 쓴 참조 해제
        return result;
    }
    // ...
}
```

```
warning: [unchecked] unchecked cast
found: Object, required: E
        E result = elements[--size];
                            ^
```

마지막으로 실체화 불가 타입인 `E`의 형변환이 안전함을 직접 증명하고 경고를 숨겨야 됩니다.

```java
public class Stack<E> {
    // ...
    public E pop() {
        if (size == 0)
            throw new EmptyStackException();
        
        // push에서 E 타입만 허용하므로 이 형변환은 안전하다. 
        @SuppressWarnings("unchecked") E result = (E) elements[--size];
        
        elements[size] = null; // 다 쓴 참조 해제
        return result;
    }
    // ...
}
```

### 첫 번째 방식과 두 번째 방식의 비교

두 방식 모두 나름의 지지를 얻고 있습니다.

첫 번째 방식은 가독성이 좋고 코드가 짧습니다. 형변환을 배열 생성시 단 한번만 되지만, 두 번째 방식은 배열에서 원소를 읽을 때마다 해줘야 됩니다. 그래서 현업에서는 
첫 번째 방식을 주로 사용합니다.
하지만 `E`가 `Object`가 아니라면 배열의 런타임 타입이 컴파일 타입과 달라 첫 번째 방식을 사용하면 힙 오염이 발생합니다. 그래서 힙 오염이 걱정되는 개발자들은 두
번째 방식을 고수합니다. 

##### 제네릭 `Stack`을 사용하는 맛보기 프로그램

```java
public class GenericStackTest {

    @Test
    void test() {
        // given
        String[] args = new String[3];
        for (int i = 0; i < 3; i++)
            args[i] = String.valueOf((char) ('a' + i));

        // when
        Stack<String> stack = new Stack<>();
        for (String arg : args)
            stack.push(arg);

        // then
        while (!stack.isEmpty())
            System.out.println(stack.pop().toUpperCase());
    }
}
```

`Stack`에서 꺼낸 원소에서 `String`의 `toUpperCase` 메서드를 호출할 때 명시적 형변환을 수행하지 않으며, 이 형변환이 항상 성공함을 보장합니다.

## 대다수의 제네릭 타입은 타입 매개변수에 제약을 두지 않는다

`Stack<Object>`, `Stack<int[]>`, `Stack<List<String>>`, `Stack` 등 어떤 참조 타입으로도 `Stack`을 만들 수 있지만, `Stack<int>`, 
`Stack<double>`과 같은 기본 타임은 사용할 수 없습니다. 이는 자바 제네릭 타입 시스템의 근본적인 문제이며, 박싱된 기본 타입을 사용하여 우회할 수 있습니다.

## 타입 매개변수에 제약을 두는 제네릭 타입도 있다

`java.util.concurrent.DelayQueue`는 다음처럼 선언되어 있습니다.

```java
class DelayQueue<E extends Delayed> implements BlockingQueue<E> {}
```

`<E extends Delayed>`는 `java.util.concurrent.Delayed`의 하위 타입만 받는 다는 뜻입니다. `DelayQueue`의 원소에서 바로 `Delayed` 메소드를 
호출할 수 있고 `ClassCastException`을 걱정할 필요가 없습니다.


## 정리

클라이언트에서 직접 형변환해야 하는 타입보다 제네릭 타입이 더 안전하고 쓰기 편합니다. 새로운 타입을 설계할 때는 제네릭 타입을 만들어 형변환 없이도 사용할 수 있도록 
하는게 좋습니다. 기존 타입 중 제네릭이 있어야 하는게 있다면, 제네릭 타입으로 변경하여 기존 클라이언트에는 아무 영향을 주지 않으면서 새로운 사용자를 훨씬 편하게 
해줘야 됩니다.