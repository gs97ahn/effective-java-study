# 배열보다는 리스트를 사용하라

## 배열과 제네릭 타입의 주 차이점

1. 배열은 공변이고 제네릭은 불공변이다.
2. 배열은 실체화 된다.

### 배열은 공변이고 제네릭은 불공변이다

#### 공변

`Sub`가 `Super`의 하위 타입이라면 배열 `Sub[]`는 `Super[]`의 하위타입이다.

#### 불공변

서로 다른 타입 `Type1`과 `Type2`가 있을 때, `List<Type1>`은 `List<Type2>`의 하위 타입도 아니고 상위 타입도 아니다.

##### 테스트

```java
public class TypeTest {

    @Test
    public void runtimeFailTest() {
        Object[] objectArray = new Long[1];
        objectArray[0] = "타입이 달라 넣을 수 없다."; // ArrayStoreException을 던진다.
    }
    
    @Test
    public void compileFailTest() {
        List<Object> ol = new ArrayList<Long>(); // 호환되지 않는 타입이다.
        ol.add("타입이 달라 넣을 수 없다.");
    }
}
```

##### 결과 

`runtimeFailTest()`

```
java.lang.ArrayStoreException: java.lang.String
```

`compileFailTest()`

```
error: incompatible types: ArrayList<Long> cannot be converted to List<Object>
        List<Object> ol = new ArrayList<Long>();
```

결과의 두 케이스 모두 `Long`용 저장소에 `String`을 넣을 수 없다. 하지만 배열은 런타임에 실수를 알 수 있고 컴파일에 실수를 알 수 있다.

### 배열은 실체화 된다

배열은 런타임에 자신이 담기로 한 원소의 타입을 인지하고 확인한다. 그래서 `Long` 배열에 `String`을 넣으려 하면 `ArrayStoreException`이 발생한다. 반면 
제네릭은 컴파일시 타입을 인지하고 확인하기 때문에 타입 정보가 런타임에는 소거된다. 

### 배열과 제네릭은 잘 어우러지지 못한다

배열은 제네릭 타입, 매개변수화 타입, 타입 매개변수로 사용할 수 없다. 즉 `new List<E>[]`, `new List<String>[]` `new E[]`식으로 작성하면 컴파일할 
때 제네릭 배열 생성 요류를 일으킨다. 재네릭 배열을 만들지 못하게 만든 이유는 타입 안전하지 않기 때문이다. 이를 허용하면 컴파일러가 자동 생성한 형변환 코드에서 
런타임에 `ClassCastExcetpion`이 발생할 수 있으며 제네릭 타입 시스템의 취지와 어긋나기 때문이다.

### 실체화 불가 타입 (non-reifiable type)

`E`, `List<E>`, `List<String>` 같은 타입을 실체화 불가 타입이라 한다. 즉 실체화되지 않아서 런타임에는 컴파일타임보다 타입 정보를 적게 가지는 타입이다. 
소거 매커니즘 때문에 매개변소화 타입 가운데 실체화 될 수 있는 타입은 `List<?>`와 `Map<?,?>` 같은 비한정적 와일드카드 타입뿐이다. 배열을 비한정적 와일드카드 
타입으로 만들 수는 있지만, 유용하게 쓰일 일은 거의 없다.

### 배열을 제네릭으로 만들 수 없어서 일어나는 일

제네릭 컬렉션에서는 자신의 원소 타입을 담은 배열을 반환하는 게 보통은 불가능하다. 그리고 제네릭 타입과 가변인수 메서드를 함께 쓰면 해석하기 어려운 경고 메세지를 
받게 된다. 이는 가변인수 메서드를 호출할 때마다 가변인수 매개변수를 담을 배열이 하나 만들어지는데, 이때 그 배열의 원소가 실체화 불가 타입이라면 경고가 발생하는 
것이다. 이 문제는 `@SafeVarargs`로 대처할 수 있다.

배열로 형변환할 때 제네릭 배열 생성 오류나 비검사 형변환 경고가 뜨는 경우 `E[]` 대신 `List<E>`로 해결하면 된다. 코드가 조금 복잡해지고 성능이 살짝 나빠질 수 
있지만 타입 안정성과 상호운용성이 좋아진다.

##### 제네릭을 시급히 적용해야 되는 예시다

```java
public class ChooserTest {
    public class Chooser {
        private final Object[] choiceArray;

        public Chooser(Collection choices) {
            choiceArray = choices.toArray();
        }

        public Object choose() {
            Random rnd = ThreadLocalRandom.current();
            return choiceArray[rnd.nextInt(choiceArray.length)];
        }
    }

    @Test
    void test() {
        Object[] objects = new Object[2];
        objects[0] = "true";
        objects[1] = false;

        Chooser chooser = new Chooser(List.of(objects));

        while (true) {
            System.out.println((String) chooser.choose());
        }
    }
}
```

##### 결과

```
java.lang.ClassCastException: class java.lang.Boolean cannot be cast to class java.lang.String
```

`choose()`를 호출할 때마다 `Object`를 원하는 타입으로 형변환해야 되고 타입이 다른 원소가 있으면 런타임에 형변환 오류가 발생한다.

##### 제네릭으로 만들기 위한 첫번째 시도 - 컴파일 되지 않는다.

다음은 첫 시도다.

```java
public class Chooser<T> {
    private final T[] choiceArray;

    public Chooser(Collection<T> choices) {
        choiceArray = choices.toArray();
    }

    public Object choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceArray[rnd.nextInt(choiceArray.length)];
    }
}
```

##### 결과

```
error: incompatible types: Object[] cannot be converted to T[]
            choiceArray = choices.toArray();
                                         ^
```

생성자에서 `Object[]`를 `T[]`로 변환할 수 있기 때문에 컴파일에 오류가 발생한다.

##### 제네릭으로 만들기 위한 두번째 시도 - 경고가 발생한다.

```java
public class Chooser<T> {
    private final T[] choiceArray;

    public Chooser(Collection<T> choices) {
        choiceArray = (T[]) choices.toArray();
    }

    public Object choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceArray[rnd.nextInt(choiceArray.length)];
    }
}
```

##### 결과

```
warning: [unchecked] unchecked cast
            choiceArray = (T[]) choices.toArray();
                                               ^
```

`T`가 무슨 타입인지 알 수 업으니 컴파일러는 이 형변환이 런타입에도 안전한지 보장할 수 없다는 경고다.

##### 타입 안정성 확보

비검사 형변환 경고를 제거하려면 배열 대신 리스트를 쓰면 된다. 코드양은 이전 코드보다 조금 더 늘었고 조금 더 느릴 테지만, 런타임에 `ClassCastException`을 
만날 일은 없다.

```java
public class Chooser<T> {
    private final List<T> choiceList;

    public Chooser(Collection<T> choices) {
        choiceList = new ArrayList<>(choices);
    }

    public T choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceList.get(rnd.nextInt(choiceList.size()));
    }
}

```

## 정리

배열은 공변이고 실체화 되는 반면 제네릭은 불공변이고 타입 정보가 소거된다. 그래서 배열은 런타임에는 타입 안전하지만 컴파일 타임에는 안전하지 않다. 제네릭은 반대다. 
배열과 제네릭을 섞어 쓰다가 컴파일 오류나 경고를 만나면 먼저 배열을 리스트로 대체하자.