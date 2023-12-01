# 배열보다는 리스트를 사용하라

## 배열과 제네릭 타입의 주 차이점

1. 배열은 공변이고 제네릭은 불공변입니다.
2. 배열은 실체화 됩니다.

### 배열은 공변이고 제네릭은 불공변이다

#### 공변

`Sub`가 `Super`의 하위 타입이라면 배열 `Sub[]`는 `Super[]`의 하위타입입니다.

#### 불공변

서로 다른 타입 `Type1`과 `Type2`가 있을 때, `List<Type1>`은 `List<Type2>`의 하위 타입도 아니고 상위 타입도 아닙니다.

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

결과의 두 케이스 모두 `Long`용 저장소에 `String`을 넣을 수 없습니다. 배열은 런타임에 실수를 알게 되고, 리스트는 컴파일 타임에 실수를 알 수 있습니다.

## 배열은 실체화 된다

배열은 런타임에 자신이 담기로 한 원소의 타입을 인지하고 확인합니다. 그래서 `Long` 배열에 `String`을 넣으려 하면 `ArrayStoreException`이 발생합니다. 
반면 제네릭은 컴파일시 타입을 인지하고 확인하기 때문에 타입 정보가 런타임에는 소거됩니다.

## 배열과 제네릭은 잘 어우러지지 못한다

배열은 제네릭 타입, 매개변수화 타입, 타입 매개변수로 사용할 수 없습니다. 즉 `new List<E>[]`, `new List<String>[]` `new E[]`식으로 작성하면 
컴파일시 제네릭 배열 생성 오류가 발생합니다. 제네릭 배열을 만들지 못하게 만든 이유는 타입 안정성 때문입니다. 제네릭 배열을 만들게 해주면 컴파일러가 자동 생성한 
형변환 코드에서 `ClassCastException`이 런타임에 발생할 수 있고, 제네릭 타입 시스템의 취지와 어긋나기 때문입니다.

## 실체화 불가 타입 (non-reifiable type)

`E`, `List<E>`, `List<String>` 같은 타입을 실체화 불가 타입이라 합니다. 즉 실체화되지 않아서 런타임에는 컴파일 타임보다 타입 정보를 적게 가지는 
타입입니다. 소거 매커니즘 때문에 매개변수화 타입 가운데 실체화 될 수 있는 타입은 `List<?>`와 `Map<?,?>` 같은 비한정적 와일드카드 타입뿐입니다. 배열을 
비한정적 와일드카드 타입으로 만들 수는 있지만, 유용하게 쓰일 일은 거의 없습니다.

## 배열을 제네릭으로 만들 수 없어서 일어나는 일

제네릭 컬렉션에서는 자신의 원소 타입을 담은 배열을 반환하는 게 보통은 불가능합니다. 그리고 제네릭 타입과 가변인수 메서드를 함께 쓰면 해석하기 어려운 경고 메세지를 
받게 됩니다. 이는 가변인수 메서드를 호출할 때마다 가변인수 매개변수를 담을 배열이 하나 만들어지는데, 이때 그 배열의 원소가 실체화 불가 타입이라면 경고가 발생하는 
것입니다. 이 문제는 `@SafeVarargs`로 대처할 수 있습니다.

배열로 형변환할 때 제네릭 배열 생성 오류나 비검사 형변환 경고가 뜨는 경우 `E[]` 대신 `List<E>`로 해결하면 됩니다. 코드가 조금 복잡해지고 성능이 살짝 나빠질 
수 있지만 타입 안정성과 상호운용성이 좋아집니다.

##### 제네릭을 시급히 적용해야 되는 예시

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

```java
public class Box {
    private int x;
    private int y;

    public Box(int x, int y) {
        this.x = x;
        this.y =y;
    }

    void size() {
        System.out.println("x=" + x + ", y=" + y);
    }
}
```

```java
public class ChooserTest {
    @Test
    void test() {
        // given
        List<Object> list = List.of(new Box(), false);
        Chooser chooser = new Chooser(list);

        // when & then
        while (true) {
            Box box = (Box) chooser.choose();
            box.size();
        }
    }
}
```

##### 결과

```
java.lang.ClassCastException: class java.lang.Boolean cannot be cast to class Box
```

`choose()`를 호출할 때마다 `Object`를 원하는 타입으로 형변환해야 되고 타입이 다른 원소가 있으면 런타임에 형변환 오류가 발생합니다.

##### 제네릭으로 만들기 위한 첫번째 시도 - 컴파일 되지 않는다.

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

생성자에서 `Object[]`를 `T[]`로 변환할 수 있기 때문에 컴파일에 오류가 발생합니다.

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

`T`가 무슨 타입인지 알 수 없으니 컴파일러는 이 형변환이 런타입에도 안전한지 보장할 수 없다는 경고입니다.

##### 타입 안정성 확보

비검사 형변환 경고를 제거하려면 배열 대신 리스트를 쓰면 됩니다. 코드양은 이전 코드보다 조금 더 늘었고 조금 더 느릴 테지만, 런타임에 `ClassCastException`을 
만날 일은 없습니다.

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

배열은 공변이고 실체화 되는 반면 제네릭은 불공변이고 타입 정보가 소거됩니다. 그래서 배열은 런타임에는 타입 안전하지만 컴파일 타임에는 안전하지 않습니다. 제네릭은 
이에 반대입니다. 배열과 제네릭을 섞어 쓰다가 컴파일 오류나 경고를 만나면 먼저 배열을 리스트로 대체해야 됩니다.