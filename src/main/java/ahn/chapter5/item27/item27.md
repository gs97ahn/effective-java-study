# 비검사 경고를 제거하라.

## 개요

제네릭을 사용하면 수많은 컴파일 경고(비검사 형변환 경고, 비검사 메서드 호출 경고, 비검사 매개변수화 가변인수 타입 경고, 비검사 변환 경고 등)를 마주한다. 하지만 
대부분의 비검사 경고는 쉽게 제거할 수 있다.

## 가능한 모든 비검사 경고를 제거하자

모든 경고를 제거하면 타입 안정성을 보장할 수 있다. 즉, 런타임에 `ClassCastException`이 발생할 확률이 없고 잘못된 코드를 컴파일타임에 발견할 수 있다.

아래의 예시 코드을 작성하면 경고문이 뜨는걸 볼 수 있다.

##### 예시

```java
Set<Lark> exaltation = new HashSet();
```

##### 경고문

```
warning: [unchekced] unchecked conversion
Set<Lark> exaltation = new HashSet();
                        ^
required: Set<Lark>
foudn:    HashSet
```

컴파일러가 알려준대로 수정을 하거나, 자바 7부터 지원하는 다이아몬드 연산자(<>)로 해결할 수 있다.

````java
Set<Lark> exaltation = new HashSet<>();
````

## 제거를 할 수 없는 경고는 `@SuppressWarnings("unchecked")`로 경고를 숨기자

#### 주의 사항

- 타입 안전함을 검증하지 않고 경고를 숨기면 잘못된 보안 인식을 심어주게 된다. 그리고 컴파일은 문제 없이 되겠지만 런타임에 `ClassCastException`을 던질 수 
있기에 치명적이다.
- 타입 안전함을 검증한 후에 경고를 제거하지 않으면 새로운 경고가 나와도 눈치채지 못할 수 있다.
- 지역변수부터 클래스 전체까지 선언할 수 있지만 최대한 좁은 범위에 적용해야 한다.
  - 한 줄이 넘는 메서드나 생성자에 달지말고 지역변수 선언 쪽으로 옮겨야 된다.

##### 예시

```java
public <T> T[] toArray(T[] a) {
    if (a.length < size)
        return (T[]) Arrays.copyOf(elements, size, a.getClass());
    System.arraycopy(elements, 0, a, 0, size);
    if (a.length > size)
        a[size] = null;
    return a;
}
```

##### 경고문

```
warning: [unchecked] unchecked cast
return (T[]) Arrays.copyOf(elements, size, a.getClass());
                            ^
required: T[]
found:    Object[]
```

return문에는 `@SupressWarnings`를 다는 게 불가능하다. 이때 메서드 전체에 달면 범위가 필요 이상으로 넓어지니 반환값을 담을 지역변수를 하나 선언하고 그 
변수에 애너테이션을 달자.

##### 지역변수를 추가해 `@SuppressWarnings`의 범위를 좁힌다.

```java
public <T> T[] toArray(T[] a) {
    if (a.length < size) {
        @SuppressWarnings("unchecked") T[] result = (T[]) Arrays.copyOf(elements, size, a.getClass());
        return result;
    }
    System.arraycopy(elments, 0, a, 0, size);
    if (a.length > size)
        a[size] = null;
    return a;
}
```

`@SuppressWarnings("unchecked")` 애너테이션을 사용하면 그 경고를 무시해도 안전 이유를 항상 주석으로 남겨야 한다.

## 정리

비검사 경고을 무시하면 런타임에서 `ClassCastException`이 일어날 수 있으니 최대한 전부 제거를 해야한다. 만약 경고를 없앨 수 없으면 코드가 타입 안전함을 
증명하고 범위를 가능한 최대로 좁혀 `@SuppressWarnings("unchecked")`로 경고를 숨겨야 된다.