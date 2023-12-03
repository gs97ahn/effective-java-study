# 이왕이면 제네릭 메서드로 만들라

클래스처럼 메서드도 제네릭으로 만들 수 있습니다. 매개변수화 타입을 받는 정적 유틸리티 메서드는 대부분 제네릭입니다.

## 제네릭 메서드 작성법

제네릭 메서드를 작성하는건 제네릭 타입을 작성하는 것과 비슷합니다.

##### raw 타입 사용 - 수용 불가!

```java
public static Set union(Set s1, Set s2) {
    Set result = new HashSet(s1);
    result.addAll(s2);
    return result;
}
```

##### 결과

```
warning: [unchecked] unchecked call to HashSet(Collection<? extends E>) as a memeber of raw type HashSet
    Set result = new HashSet(s1);
                 ^
warning: [unchecked] unchecked call to HashSet(Collection<? extends E>) as a memeber of raw type HashSet
    Set result = new HashSet(s2);
                 ^
```

경고를 없애기 위해서 이 메서드의 타입을 안전하게 만들어야 됩니다. 메서드 선언에서의 세 집합(입력 2개, 반환 1개)의 원소 타입을 타입 매개변수로 명시하고, 메서드 
안에서도 이 타입 매개변수만 사용하게 수정하면 됩니다. 타입 매개변수들을 선언하는 타입 매개변수 목록은 메서드의 제한자와 반환 타입 사이에 있습니다. 아래의 코드에서 
타입 매개변수 목록은 `<E>`이고 반환 타입은 `Set<E>`입니다.

##### 제네릭 메서드

```java
public class UnionTest {

    public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
        Set<E> result = new HashSet<>(s1);
        result.addAll(s2);
        return result;
    }

    @Test
    void test() {
        // given
        Set<String> guys = Set.of("톰", "딕", "해리");
        Set<String> stooges = Set.of("래리", "모에", "컬리");

        // when & then
        Set<String> aflCio = union(guys, stooges);
        System.out.println(aflCio);
    }
}
```

##### 결과

```
[톰, 해리, 래리, 딕, 컬리, 모에]
```

이 메서드는 경고 없이 컴파일 되며, 타입 안전하고, 쓰기도 쉽기 때문에 단순한 제네릭 메서드라면 이정도면 충분합니다.

## 불변 객체를 여러 타입으로 활용하는 방법

제네릭은 런타임에 타입 정보가 소거되기 때문에 하나의 객체를 어떤 타입으로든 매개변수화할 수 있습니다. 이를 위해서 요청한 타입 매개 변수와 맞게 매번 그 객체의 타입을
바꿔주는 정적 팩터리를 만들어야 되고, 이 패턴은 제네릭 싱글턴 팩터리라합니다.

## 항등함수를 담은 클래스

자바 라이브러리에 있는 `Function.identity`를 사용하면 항등함수(identity function)를 만들 수 있습니다. 자바의 제네릭이 실체화 된다면 항등함수를 
타입별로 하나씩 만들어야 됩니다. 하지만 소거 방식을 사용한 덕에 제네릭 싱글턴 하나만 활용하면 됩니다. 이를 통해 객체를 요청할 때마다 새로 생성하지 않을 수 
있습니다.

## 제네릭 싱글턴 팩터리 패턴

```java
private static UnaryOperator<Object> IDENTITY_FN = (t) -> T;

@SuppressWarnings("unchecked")
public static <T> UnaryOperator<T> identityFunction() {
    return (UnaryOperator<T>) IDENTITY_FN;
}
```

`IDENTITY_FN`은 `UnaryOperator<T>`로 형변환하면 비검사 형변환 경고가 발생합니다. 이는 `T`가 어떤 타입이든 `UnaryOperator<Object>`는 
`UnaryOperator<T>`가 아니기 때문입니다. 항등함수는 입력 값을 수정 없이 그대로 반환하는 특별한 함수이기 때문에 `T`가 어떤 타입이든 
`UnaryOperator<T>`를 사용해도 타입 안전합니다. 그러므로 메서드가 내보내는 비검사 형변환 경고는 숨겨도 괜찮습니다.

##### 재네릭 싱글턴을 사용하는 예

아래와 같이 제네릭 싱글턴을 `UnaryOperator<String>`과 `UnaryOperator<Number>` 같이 사용하면 형변환을 하지 않아도 컴파일 오류나 경고가 발생하지
않습니다.

```java
public class UnaryOperatorTest {

    @Test
    void test() {
        String[] strings = { "삼베", "대마", "나일론" };
        UnaryOperator<String> sameString = UnaryOperator.identity();
        for (String s : strings)
            System.out.println(sameString.apply(s));

        Number[] numbers = { 1, 2.0, 3L };
        UnaryOperator<Number> sameNumber = UnaryOperator.identity();
        for (Number n : numbers)
            System.out.println(sameNumber.apply(n));
    }
}
```

##### 결과

```
삼베
대마
나일론
1
2.0
3
```

## 자기 자신이 들어간 표현식을 사용하여 타입 매개변수의 허용 범위를 한정하는 방법

재귀적 타입 한정(recursive type bound)이라는 개념은 자기 자신이 들어간 표현식을 사용하여 타입 매개변수의 허용 범위를 한정할때 사용된다. 재귀적 타입 한정은 
타입의 자연적 순서를 정하는 `Comparable` 인터페이스와 함께 사용됩니다.

아래의 예시에서 타입 매개변수 `T`는 `Comparable<T>`를 구현한 타입이 비교할 수 잇는 원소의 타입을 정의합니다. 거의 모든 타입은 자신과 같은 타입의 원소와만 
비교할 수 있어서 `String`은 `Comparable<String>`을 구현하고 `Integer`는 `Comparable<Integer>`을 구현합니다.

```java
public interface Comparable<T> {
    int compareTo(T o);
}
```

`Comparable`을 구현한 원소의 컬렉션을 입력받는 메서드들은 보통 원소들을 정렬 혹은 검색하거나, 최솟값이나 최댓값을 구하는 식으로 사용됩니다. 이를 위해서 컬렉션에
담긴 모든 원소가 상호 비교될 수 있어야 됩니다.

##### 재귀적 타입 한정을 이용해 상호 비교할 수 있음을 표현했다.

```java
public static <E extends Comparable<E>> E max(Collection<E> c);
```

`<E extends Comparable<E>>`는 모든 타입 E는 자신과 비교할 수 있다는걸 뜻합니다. 즉, 상호 비교가 가능하다는 뜻입니다.

##### 컬렉션에서 최대값을 반환한다. - 재귀적 타입 한정 사용

이 메서드는 컬렉션에 담긴 원소의 자연적 순서를 기준으로 최댓값을 계산하며, 컴파일 오류나 경고가 발생시키지 않습니다.

```java
public class MaxTest {

    public static <E extends Comparable<E>> E max(Collection<E> c) {
        if (c.isEmpty())
            throw new IllegalArgumentException("컬렉션이 비어 있습니다.");

        E result = null;
        for (E e : c)
            if (result == null || e.compareTo(result) > 0)
                result = Objects.requireNonNull(e);

        return result;
    }

    @Test
    void stringTest() {
        // given
        List<String> strings = List.of("가", "나", "다");

        // when & then
        String max = max(strings);
        System.out.println(max);
    }

    @Test
    void intTest() {
        // given
        List<Integer> integers = List.of(1, 2, 3);

        // when & then
        int max = max(integers);
        System.out.println(max);
    }
}
```

##### 결과

`stringTest()`

```java
다
```

`intTest()`

```java
3
```

## 정리

제네릭 타입처럼 클라이언트에서 입력 매개변수와 반환값을 명시적으로 형변환해야 하는 메서드보다, 제네릭 메서드가 더 안전하고 사용하기도 쉬운 편입니다. 타입처럼 메서드도 
형변환 없이 사용할 수 있는 편이 좋습니다. 이를 위해 제네릭 메서드를 만들면 됩니다. 그리고 기존에 존재하는 메서드 중 형변환이 필요한 메서드는 제네릭하게 변경하는게 
좋습니다.
