# clone 재정의는 주의해서 진행하라
- `Cloneable`은 복제해도 되는 클래스임을 명시하는 용도의 믹스인 인터페이스지만, 목적을 제대로 이루지 못함
  - 문제
    - `clone` 메서드가 선언된 곳이 `Cloneable`이 아닌 `Object`이고 그마저도 `protected`라는 데 있음
    - `Cloneable`을 구현하는 것만으로는 외부 객체에서 `clone` 메서드를 호출하지 못함
    - 리플렉션을 사용하면 가능하지만 해당 객체가 접근이 허용된 clone 메서드를 제공한다는 보장 없음

### 메서드 하나 없는 Cloneable
```java
public interface Cloneable {
}
```
- `Object`의 `protected` 메서드인 `clone`의 동작 방식을 결정
  - `@HotSpotIntrinsicCandiate`: [^1]
    - HotSpot VM에 내재화 될 수도 있고 아닐수도 있다는 의미
  - `native`: [^2]
    - `JNI(Java Native Interface)`를 활용해서 구현됐다는 것을 표시
    - 메소드에서만 사용 가능
    - 주로 C, C++로 구현됨
```java
public class Object {
    // ...
    @HotSpotIntrinsicCandiate
    protected native Object clone() throws CloneNotSupportedException;        
    // ...
}
```
- `Cloneable`을 구현한 클래스에서 `clone` 호출 -> 그 객체의 필드들을 전부 복사한 객체를 반환
- `Cloneable`을 구현하지 않은 클래스에서 `clone` 호출 -> `CloneNotSupportedException`을 던짐
  - 이례적으로 사용한 예시라 따라하기 안됨

### 실무에서의 `Cloneable`
- 구현한 클래스는 `clone` 메서드를 `public`으로 제공하며, 당연히 복제가 제대로 이뤄진다는 기대를 함
  - 그 클래스와 모든 상위 클래스는
    - 복잡함
    - 강제할 수 없음
    - 허술함
  - 결과적으로
    - 깨지기 쉬움
    - 위험함
    - 모순적임
  - 이유
    - 생성자를 호출하지 않고 객체 생성 가능

## `clone` 규약
- `clone`은 객체의 복사본을 생성해 반환
- 아래의 식은 일반적으로 참임, 그리고 이 이상 반드시 만족해야 하는 것은 아님
```
x.clone() != x
x.clone().getClass() == x.getClass()
```
- 아래의 식은 일반적으로 참이지만 필수는 아님
```
x.clone().equals(x)
```
- 관례
  - `clone`이 반환하는 객체는 `super.clone`을 호출해 얻음
    - 생성자 연쇄와 같이 구현하면 안됨
      - 하위 클래스에서 `super.clone` 호출시 잘못됨
    - 클래스가 `final`이면 하위 클래스 존재하지 않아 -> 무시 가능

#### Given
```java
public class A implements Cloneable {
    private int a;
    
    public A(int a) {
        this.a = a;
    }
    
    @Override
    public Object clone() {
        return new A(this.a);
    }
}
```
```java
public class B extends A implements Cloneable {
    private int b;
  
    public B(int a, int b) {
        super(a);
        this.b = b;
    }
  
    @Override
    public Object clone() {
        return super.clone();
    }
}
```

#### When
```java
@Test
void test() {
    B original = new B();
    
    Object cloned = b.clone();
    
    System.out.println("original = " + original.getClass());
    System.out.println("cloned = " + cloned.getClass());
}
```

#### Then
```
original = class B
cloned = class A
```
  - 반환된 객체와 원본 객체는 독립적임
    - `super.clone`으로 얻은 객체의 필드 중 하나 이상을 반환 전에 수정해야 할 수도 있음

## 제대로 동작하는 `clone` 메서드를 가진 상위 클래스를 상속해 `Cloneable` 구현
- `super.clone` 호출 -> 완벽한 복제본
- 공변 반환 타이핑(covariant return typing)을 통해 `Object` 반환을 `PhoneNumber` 반환으로 수정 권장
- try-catch 블록으로 감싼 이유 -> `CloneNotSupportedException`을 던지도록 선언
  - `CloneNotSupportedException`은 사실 비검사 예외
```java
public final class PhoneNumber implements Cloneable {
    private final short areaCode, prefix, lineNum;
  
    public PhoneNumber(int areaCode, int prefix, int lineNum) {
        this.areaCode = rangeCheck(areaCode, 999, "지역코드");
        this.prefix = rangeCheck(prefix, 999, "프리픽스");
        this.lineNum = rangeCheck(lineNum, 9999, "가입자 번호");
    }
  
    private static short rangeCheck(int val, int max, String args) {
        if (val < 0 || val > max)
            throw new IllegalArgumentException(args + ": " + val);
        return (short) val;
    }
  
    @Override
    public PhoneNumber clone() {
      try {
        return (PhoneNumber) super.clone();
      } catch (CloneNotSupportedException e) {
        throw new AssertionError(); // 일어날 수 없는 일이다.
      }
    }
}
```

## 구현 클래스가 가변객체를 참조
- 가변객체가 있을 경우 -> `super.clone`시
  - `elements`가 같은 배열을 참조

#### Given
```java
public class Stack implements Cloneable {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
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
  
    // 원소를 위한 공간을 적어도 하나 이상 확보한다.
    private void ensureCapacity() {
        if (elements.length == size) 
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }
    
    @Override
    public Stack clone() {
        try {
            return (Stack) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
```

#### When
```java
@Test
void test() {
    Stack original = new Stack();
    Stack cloned = original.clone();

    System.out.println("original.elements = " + original.elements);
    System.out.println("cloned.elements = " + cloned.elements);
}
```

#### Then
```
original.elements = [Ljava.lang.Object;@3d24753a
cloned.elements = [Ljava.lang.Object;@3d24753a
```
- `clone` 메서드는 
  - 생성자와 같은 효과
  - 원본 객체에 아무런 해를 끼치지 않는 동시 복제된 객체의 불변식 보장
- 배열에서 `clone` 기능은 유일하게 `clone`을 제대로 사용시 형변환 필요하지 않음
```java
public class Stack implements Cloneable {
    // ...
    @Override
    public Stack clone() {
        try {
            Stack result = (Stack) super.clone();
            result.elements = elements.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    // ...
}
```
- 만약 `elements` 필드가 `final` 이었다면 -> 위 방식은 제대로 작동하지 않음
  - `Cloneable` 아키텍처는 '가변 객체를 참조하는 필드는 `final`로 선언하라'는 일반 용법과 충돌
    - 단, 원본과 복제된 객체가 그 가변 객체를 공유해도 안전하면 괜찮음
  - 복제할 수 있는 클래스 만들기 위해 일부 필드에서 `final` 제거

## `clone`을 재귀적으로 호출하는 것만으로 충분하지 않을 때
```java
public class HashTable implements Cloneable {
    private Entry[] buckets = {};
    
    private static class Entry {
        final Object key;
        Object value;
        Entry next;
        
        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
    // ...
}
```

#### 잘못된 `clone`메서드 - 가변 상태 공유
```java
public class HashTable implements Cloneable {
    @Override
    public HashTable clone() {
        try {
            HashTable result = (Hashtable) super.clone();
            result.buckets = buckets.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
      }
}
```

#### 복잡한 가변상태를 갖는 클래스용 `clone` 메서드 (재귀적 & 순회 & ...)
##### 재귀적 방법
- 너무 길지 않으면 👍🏻
- 길면 스택 오버플로를 일으킬 수 있어 👎🏻
```java
public class HashTable implements Cloneable {
    private Entry[] buckets = {};
    
    private static class Entry {
        final Object key;
        Object value;
        Entry next;
        
        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
        
        // 이 엔트리가 가르키는 연결 리스트를 재귀적으로 복사
        Entry deepCopy() {
            return new Entry(key, value, next == null ? null : next.deepCopy());
        }
    }
    
    
    @Override
    public HashTable clone() {
        try {
            HashTable result = (Hashtable) super.clone();
            result.buckets = new Entry[buckets.length];
            for (int i = 0; i < buckets.length; i++)
                if (buckets[i] != null) 
                    result.buckets[i] = buckets[i].deepCopy();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    // ...  
}
```

##### 순회 방법
- 길어도 스택 오버플로가 안일어나 추천
```java
public class HashTable implements Cloneable {
    // ...
    private static class Entry {
        // ..
        Entry deepCopy() {
            Entry result = new Entry(key, value, next);
            for (Entry p = result; p.next != null; p = p.next)
                p.next = new Entry(p.next.key, p.next.value, p.next.next);
            return result;
        }
    }
    // ...
}
```

##### 추가 방법
1. `super.clone` 호출해 얻은 객체의 모든 필드를 초기 상태로 설정
2. 원본 객체의 상태를 다시 생성하는 고수준 메서드들을 호출
   - `HashTable` 인 경우 키-값 쌍을 각각 복제해 `put(key, value)` 메서드를 호출하여 동일하게 만듬
- 고수준 API 활용 복제 -> 간단하고 우아한 코드
- 저수준보다 느림
- `Cloneable`과 어울리지 않음
  - 필드 단위 객체 복사를 우회하기 때문

## `clone` 메서드에서 재정의될 수 있는 메서드 호출하면 안됨
- 하위 클래스에서 재정의한 메서드 호출 -> 하위 클래스는 복제 과정에서 자신의 상태를 교정할 기회를 잃음 -> 원본과 복제본의 상태가 달라질 수 있음
- `put(key value)`를 통해 `clone` 호출을 원한다면
  - 메서드가 `final` 또는 `private`이어야 됨

## `public`인 `clone` 메서드에서는 `throws`절을 제거해야 됨
- 검사 예외를 던지지 않아야 메서드 사용이 편하기 때문

## 상속해서 쓰기 위한 클래스 설계 방식 두 가지 중 어느 쪽에서든, 상속용 클래스는 `Cloneable`을 구현해서는 안됨
### 방법
1. `Cloneable` 구현 여부를 하위 클래스에서 선택
2. `clone`을 동작하지 않게 구현해놓고 하위 클래스에서 재정의하지 못하게
```java
@Override
protected final Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
}
```

## 기억해야될 사항
- `Cloneable`을 구현한 스레드 안전 클래스를 작성시 `clone` 메서드 역시 적절히 동기화 필요
  - `Object`의 `clone` 메서드는 동기화 신경쓰지 않음 -> `super.clone` 호출 외 다른 할일이 없어도 재정의하고 동기화 해줘야됨
- 위의 모든 작업들이 반드시 필요?
  - 위의 예들처럼 복잡한 경우는 드물음
  - `Cloneable`을 이미 구현한 클래스를 확장 -> 위의 방법이 필요
  - `Cloneable`을 구현 안한 클래스를 확장 -> 복사 생성자와 복사 팩터리라는 더 나은 객체 복사 방식 제공 가능

##### 복사 생성자
```java
public Yum(Yum yum) {
    // ...
};
```

##### 복사 팩터리
```java
public static Yum newInstance(Yum yum) {
    // ...
};
```

### 복사 생성자와 그변형인 복사 팩터리는 `Cloneable`/`clone` 방식보다 나은 면이 많음
  - 생성자를 쓰지 않는 방식의 객체 생성 매커니즘이 아님
  - 엉상하게 문서화된 규약에 기대 안함
  - 정상적인 `final` 필드 용법과 충돌 안함
  - 불필요한 검사 예외 없음
  - 형변환 필요 없음

[^1]: https://stackoverflow.com/questions/66842504/difference-between-native-keyword-and-hotspotintrinsiccandidate-annotation
[^2]: https://www.geeksforgeeks.org/native-keyword-java/