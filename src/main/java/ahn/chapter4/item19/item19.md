# 상속을 고려해 설계하고 문서화하라. 그러지 않았따면 상속을 금지하라

> 상속용 클래스는 재정의할 수 있는 메서드들을 내부적으로 어떻게 이용하는지 문서로 남겨야 됩니다. 구체적으로 `public`과 `protected` 메서드 중 `final`
> 이 아닌 모든 메서드들은 재정의 가능하기에 모든 상황을 문서로 남겨야됩니다.

## API 문서의 메서드 설명 "Implementation Requirements"

메서드 주석에 `@implSpec` 태그를 붙여주어 메서드의 동작 박식에 대한 설명을 작성할 수 있습니다.

##### `java.util.AbstractCollection`
```java
public abstract class AbstractCollection<E> implements Collection<E> {
    /**
     * ...
     * @implSpec 이 메서드는 컬렉션을 순회하며 주어진 원소를 찾도록 구현되었다. 주어진 원소를 찾으면 반복자의 remove 메서드를 사용해 컬렉션에서 제거한다.
     * 이 컬렉션이 주어진 객체를 갖고 있으나, 이 컬렉션의 iterator 메서드가 반환한 반복자가 remove 메서드를 구현하지 않았다면 
     * UnsupportedOperationException을 던지니 주의하자.
     */
    public boolean remove(Object o) {
        // ...
    }
    
    // ...
}
```

위의 설명에 따르면 `iterator` 메서드를 재정의하면 `remove` 메서드의 동작에 영향을 줌을 알 수 있습니다. 하지만 `HashSet`에서는 `add` 메서드 
재정의시 `addAll` 메서드에 영향을 준다는 사실을 알 수 없습니다. 이는 상속이 캡슐화를 해치기 때문에 일어나는 현상입니다. 그래서 클래스를 안전하게 
상속하려면 내부 구현 방식을 설명해야 됩니다.

### `@implSpec` 태그의 활성화
`@implSpec` 태그는 자바 8부터 도입되어 자바 9부터 사용되기 시작하였습니다. 기본값으로 활성화 되어야 될거 같지만 자바 11에서도 선택사항으로 남겨져 
있습니다. 이 태그를 기본값으로 활성화하기 위해서는 다음 명령줄 매개 변수를 지정해주면 됩니다.
```
-tag "implSPec:a Implementation Requirements:"
```

## 상속을 위한 설계
상속을 위한 설계를 하기 위해서는 쉽게 효율적인 하위 클래스를 만들 수 있어야 됩니다. 구체적으로 클래스의 내부 동작 과정 중간에 끼어들 수 있는 훅(hook)을
선별하여 `protected` 메서드 형태로 공개해야 할 수도 있습니다.

##### `java.util.AbstractList`
```java
public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {
    /**
     * fromIndex(포함)부터 toIndex(미포함)까지의 모든 원소를 이 리스트에서 제거한다. toIndex 이후의 원소들은 앞으로 (index만큼씩) 당겨진다. 이
     * 호출로 리스트는 'toIndex - fromIndex'만큼 짧아진다. (toIndex == fromIndex라면 아무런 효과가 없다.)
     *   이 리스트 혹은 리스트의 부분리스트에 정의된 clear 연산이 이 메서드를 호출한다. 리스트 구현의 내부 구조를 활용하도록 이 메서드를 재정의하면 이
     * 리스트와 부분리스트의 clear 연산 성능을 크게 개선할 수 있다.
     * @implSpec 이 메서드는 fromIndex에서 시작하는 리스트 반복자를 얻어 모든 원소를 제거할 때까지 ListIterator.next와 
     * ListIterator.remove를 반복 호출하도록 구현되어있다. 주의: ListIterator.remove가 선형 시간이 걸리면 이 구현의 성능은 제곱에 비례한다.
     * @param fromIndex 제거할 첫 원소의 인덱스
     * @param toIndex 제거할 마지막 원소의 다음 인덱스
     */
    protected void removeRange(int fromIndex, int toIndex) {
        // ...
    }
    
    // ...
}
```

`List` 구현체의 최종 사용자는 `removeRange` 메서드에 관심이 없습니다. 하지만 위의 메서드를 제공한 이유는 `clear` 메서드를 고성능으로 쉽게 만들기 
위해서입니다. 이러한 문서화 없이 `clear` 메서드를 호출하면 제거할 원소 수의 제곱에 비례해 성능이 느려집니다.

### `protected`로 노출해야하는 메서드 결정 방법

`protected` 메서드는 내부 구현에 속함으로 가능한 적어야되지만 너무 적게 노출하면 상속으로 얻는 이점을 없앨 수 있습니다.

`protected`로 노출해야 하는 메서드를 결정하는 방법은 시험을 해보는것입니다. 필요한 `protected` 멤버를 놓치게 되면 하위 클래스를 작성할 때 빈 자리가 
드러나게 됩니다. 반면 사용하지 않은 `protected` 멤버가 발견되면 `private`으로 변경해야 된다는 의미를 뜻합니다.

특히 널리 쓰일 클래스를 상속용으로 설계한다면 여러분이 문서환한 내부 사용 패턴과, `protected` 메서드와 필드를 구현하면서 선택한 결정에 영원히 책임져야 
함을 잘 인식해야 됩니다. 그래서 상속용으로 설계한 클래스는 배포 전에 꼭 하위 클래스를 만들어 검증을 해야합니다.

## 상속을 허용하는 클래스의 생성자

상속을 허용하는 클래스의 생성자는 직접 또는 간접으로 재정의 가능 메서드를 호출해서는 안됩니다. 왜냐하면 상위 클래스의 생성자가 하위 클래스의 생성자보다 먼저 
실행되기 때문에 하위 클래스에서 재정의한 메서드가 하위 클래스의 생성자보다 먼저 호출되게되어 프로그램이 오작동할 수 있습니다.

###### 상속을 허용하는 클래스의 잘못된 예시

```java
public class Super {
    // 잘못된 예 - 생성자가 재정의 가능 메서드를 호출한다.
    public Super() {
        overrideMe(); // 1-1번째
    }
    
    public void overrideMe() {}
}
```

```java
public final class Sub extends Super {
    // 초기화되지 않은 final 필드, 생성자에게 초기화한다.
    private final Instant instant;

    Sub() {
        instant = Instant.now(); // 1-3번째
    }
    
    // 재정의 가능 메서드, 상위 클래스의 생성자가 호출한다.
    @Override
    public void overrideMe() {
        System.out.println(instant); // 1-2번째, 2-1번째
    }
    
    public static void main(String[] args) {
        Sub sub = new Sub(); // 1번째
        sub.overrideMe(); // 2번째
    }
}
```

결과
```
null
2023-10-13T18:19:49.596749Z
```

결과에서 확인할 수 있듯이 `instant`를 바로 출력하지 않고 `null`을 먼저 출력하게 됩니다. 이는 초기화하기 전에 `overridMe`가 호출되기 때문입니다.

> `private`, `final`, `static` 메서드는 재정의 불가능하니 생성자에서 호출해도 괜찮습니다.

## `Cloneable`과 `Serializable` 인터페이스의 상속용 설계

`Cloneable`과 `Serializable`의 상속용 클래스는 생성자와 비슷한 제약이 있기에 주의해야됩니다. `clone`과 `readObject`는 직접 또는 간접적으로 
재정의 가능한 메서드를 호출해서는 안됩니다.

`Serializable`을 구현한 상속용 클래스가 `readResolve`나 `writeReplace` 메서드를 갖는다면 `protected`로 선언해야 됩니다. `private`으로 
구현하게 되면 하위 클래스에서 무시되기에 상속을 허용하기 위해 내부 구현을 클래스 API로 공개해야됩니다.

### `readObject`

하위 클래스의 상태가 역직렬화되기 전의 재정의한 메서드부터 호출합니다.

### `clone`

하위 클래스의 `clone` 메서드가 복제본의 상태를 올바른 상태로 수정하기 전에 재정의한 메서드를 호출합니다. 그리고 만약 복제본이 원본의 데이터를 참고하고 
있을 경우 원본 객체와 복사본 객체 둘다 피해를 입을 수 있습니다.

## 상속용이 아닌 구체 클래스

`final`도 아니고 상속용으로 설계되거나 문서화가 되지 않은 클래스는 클래스의 변화가 생길 때마다 오작동을 유발할 수 있습니다. 그러므로 상속용으로 설계되지 
않은 클래스는 상속을 금지해야됩니다.

### 상속을 금지하는 방법

1. 클래스를 `final`로 선언한다.
2. 모든 생성자를 `private` 또는 `package-private`으로 선언하고 `public` 정적 팩터리를 만들어 준다.

### 구체 클래스가 표준 잍터페이스를 구현하지 않았는데 상속을 허용하는 클래스를 만드는 방법

클래스 내부에서는 재정의 가능 메서드를 사용하지 않게 만들고 이 사실을 문서로 남겨야됩니다. 이를 통해, 메서드를 재정의해도 다른 메서드의 동작에 영향을 주지 
않기 때문입니다.