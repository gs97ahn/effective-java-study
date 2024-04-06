# 다중정의는 신중히 사용하라

## 메서드 다중정의

##### 컬렉션 분류기 - 오류! 이 프로그램은 무엇을 출력할까?

```java
public class CollectionClassifier {  
  
    public static String classify(Set<?> s) {  
        return "집합";  
    }  
  
    public static String classify(List<?> lst) {  
        return "리스트";  
    }  
  
    public static String classify(Collection<?> c) {  
        return "그 외";  
    }  
}
```


```java
public class CollectionClassifierTest {  
  
    @Test  
    void test() {  
        // given  
        Collection<?>[] collections = {  
                new HashSet<String>(),  
                new ArrayList<BigInteger>(),  
                new HashMap<String, String>().values()  
        };  
  
        // when & then  
        for (Collection<?> c : collections) {  
            System.out.println(CollectionClassifier.classify(c));  
        }  
    }  
}
```

```
그 외
그 외
그 외
```

"집합", "리스트", "그 외"이 출력 될 거라 예상되지만 "그 외"가 3번 출력 된다. 이는 다중정의(overloading, 오버로딩)은 컴파일타임에 호출할 메서드를 결정하기 때문이다. 런타임에는 타입이 달라지지만 컴파일타임에 `c`는 `Collection<?>` 타입을 가지게 돼 결과적으로 "그 외"가 3번 출력 된다.

## 메서드 재정의
##### 재정의된 메서드 호출 메커니즘 - 이 프로그램은 무엇을 출력할까?

```java
class Wine {  
    String name() {  
        return "포도주";  
    }  
}  
  
class SparklingWine extends Wine {  
    @Override String name() {  
        return "발포성 포도주";  
    }  
}  
  
class Champagne extends SparklingWine {  
    @Override String name() {  
        return "샴페인";  
    }  
}  
  
public class OverridingTest {  
  
    @Test  
    void test() {  
        List<Wine> wineList = List.of(new Wine(), new SparklingWine(), new Champagne());  
  
        for (Wine wine : wineList) {  
            System.out.println(wine.name());  
        }  
    }  
}
```

```
포도주
발포성 포도주
샴페인
```

정적으로 선택되는 다중정의한 메서드와는 다르게 재정의한 메서드는 동적으로 선택된다. 그래서 위 재정의한 메서드는 예상대로 "포도주", "발포성 포도주", "샴페인"이 순차적으로 출력된다.

## 메서드 다중정의시 발생하는 문제 해결 방법

이전에 `CollectionClassifier` 예제를 `Wine` 예제처럼 의도대로 동작하게 만들려면 메서드를 합쳐 `instanceof`로 명시해 해결할 수 있다.

```java
public class CollectionClassifier {  
  
    public static String classify(Collection<?> c) {  
        return c instanceof Set ? "집합" :  
                c instanceof List ? "리스트" : "그 외";  
    }  
}
```


```java
public class CollectionClassifierTest {  
  
    @Test  
    void test() {  
        // given  
        Collection<?>[] collections = {  
                new HashSet<String>(),  
                new ArrayList<BigInteger>(),  
                new HashMap<String, String>().values()  
        };  
  
        // when & then  
        for (Collection<?> c : collections) {  
            System.out.println(CollectionClassifier.classify(c));  
        }  
    }  
}
```

```
집합
리스트
그 외
```

## 안전하게 다중정의를 활용하는 방법

### 다중정의 대신 메서드 이름과 짝을 맞추자

가장 보수적으로 다중정의를 안전하게 활용하려면 매개변수 수가 같은 다중정의를 피하면 된다. 그리고 가변인수(varargs)를 사용하는 메서드는 아예 다중정의를 하지 말자.

그리고 다중정의 대신 메서드의 이름과 짝을 맞춰 다중정의를 피할 수 있다. `ObjectOutputStream` 클래스가 이와 같이 구현한 걸 확인할 수 있다.

```java
public class ObjectInputStream  
    extends InputStream implements ObjectInput, ObjectStreamConstants  
{
	// ...	
	public boolean readBoolean() throws IOException {}
	public byte readByte() throws IOException {}
	public int readInt()  throws IOException {}
	public long readLong()  throws IOException {}
	// ...
}
```

### 정적 팩터리를 활용하자

생성자는 메서드명을 다르게 지을 수 없기 때문에 다중정의를 할 수밖에 없다.

### 어떤 매개변수 집합을 처리할지 구분하자

매개 변수 중 하나 이상이 근본적으로 다르다면, 즉 서로 형변환할 수 없다면 헷갈릴 일 이 없다.

```java
public class SetListTest {  
  
    @Test  
    void test() {  
        // given  
        Set<Integer> set = new TreeSet<>();  
        List<Integer> list = new ArrayList<>();  
  
        for (int i = -3; i < 3; i++) {  
            set.add(i);  
            list.add(i);  
        }  
  
        // when  
        for (int i = 0; i < 3; i++) {  
            set.remove(i);  
            list.remove(i);  
        }  
  
        // then  
        System.out.println(set + " " + list);  
    }  
}
```

```
[-3, -2, -1] [-2, 0, 2]
```

"\[-3, -2, -1\] \[-3, -2, -1\]"가 출력될거 같지만 실제 출력값이 아니다. 이는 `set.remove(i)`는 `remove(Object o)`인 반면 `list.remove(i)`는 다중정의된 `remove(int index)`다. `list.remove(i)` 대신 `list.remove(Integer.valueOf(i))`로 수정하면 해결된다.

```java
public class SetListTest {  
  
    @Test  
    void test() {  
        // given  
        Set<Integer> set = new TreeSet<>();  
        List<Integer> list = new ArrayList<>();  
  
        for (int i = -3; i < 3; i++) {  
            set.add(i);  
            list.add(i);  
        }  
  
        // when  
        for (int i = 0; i < 3; i++) {  
            set.remove(i);  
            list.remove(Integer.valueOf(i)); // i로 부터 수정
        }  
  
        // then  
        System.out.println(set + " " + list);  
    }  
}
```

```
[-3, -2, -1] [-3, -2, -1]
```

## 정리

매개변수 수가 같을 때는 다중정의를 가능한 피하자. 하지만 생성자는 이를 피할 수 없기 때문에 헷갈릴만한 매개변수만 형변화해 그에 비례하는 다중정의 메서드를 선택하게 하자. 그리고 기존 클래스를 수정해 새로운 인터페이스를 만들 땐 같은 객체를 입력받는 다중정의 메서드들이 모두 동일하게 동작하게 만들자.