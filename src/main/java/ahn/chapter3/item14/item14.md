# Comparable을 구현할지 고려하라

- `compareTo`는 `Object`의 메서드가 아님
- `Object`의 `equals`와 비슷함
- `Object`의 `equals`와 차이점
  - 단순 동치성 비교
  - 순서 비교
  - 제너릭함
- `Comparable` 구현시 -> 자연적인 순서가 있음을 뜻함

##### 쉽게 정렬하는 법
- 검색
- 극단값 계산
- 자동 정렬되는 컬렉션 관리
```java
Arrays.sort(a);
```

##### 알파벳순으로 출력
```java
public class WordList {
    public static void main(String[] args) {
        Set<String> s = new TreeSet<>();
        Collections.addAll(s, args);
        System.out.println(s);
    }
}
```
- 알파벳, 숫자, 연대 같이 순서가 명확한 값 클래스 작성시 -> `Comparable` 인터페이스를 구현하자
```java
public interface Comparable<T> {
    int compareTo(T t);
}
```

## `compareTo` 규약
- 주어진 객체의 순서를 비교 후 반환
  - 작으면 음의 정수
  - 같으면 0
  - 크면 양의 정수
  - 비교할 수 없으면 `ClassCastException`
- `sgn` 표기 = 부호 함수(signum function)
  - 음수 -> -1 
  - 0 -> 0
  - 양수 -> 1
- `Comparable`을 구현한 클래스는
  - `sgn(x.compareTo(y)) == -sgn(y.compareTo(x))`
    - 하나가 예외를 던지면 다른하나도 예외 던져야됨
  - 추이성 보장
    - `(x.compareTo(y)) > 0 && (y.compareTo(z)) > 0` -> `x.compareTo(z) > 0`
      - `x > y > z` -> `x > z`
  - 모든 `z`에 대해 `x.compareTo(y) == 0` -> `x == y`
    - `sgn(x.compareTo(z)) == sgn(y.compareTo(z))`
  - 필수는 아니지만 지키면 좋은 사항
    - `(x.compareTo(y) == 0) == (x.equals(y))`
      - `Comparable`을 구현하고 지키지 않으면 사실 명시 필요
    - 주의
      - `equals` 메서드와 일관되지 않음
      - 지키지 않고 클래스의 객체를 정렬된 컬렉션에 넣으면 해당 컬렉션이 구현한 인터페이스(`Collection`, `Set`, 또는 `Map`)에 정의된 동작과 엇박자를 냄
        - 동치성 비교시 `equals` 대신 `compareTo`를 사용하기 때문
        - `BigDecimal` 클래스 예시 (`new BigDecimal("1.0)` vs `new BigDecimal("1.00")`)
          - `eqauls`시 다름
          - `compareTo`시 같음
- `equals`와 `compareTo` 차이점
  - 타입이 다르면 그냥 `ClassCastException` 던지면됨
    - 다른 타입 사이의 비교도 허용하는데 보통은 비교할 객체들이 구현한 공통 인터페이스 매개로 이뤄짐
  - `compareTo`의 인수타입 -> 컴파일 타임에 정해짐
  - `equals`의 인수타입 -> 런타임에 정해짐
- `compareTo`와 `hashCode` 비교
  - `hasCode` 규약 지키기 X -> 해시를 사용하는 클래스와 어울림 X
  - `compareTo` 규약 지키기 X -> 비교를 활용하는 클래스와 어울림 X
- 전체적으로 `compareTo` 메서드의 일반 규약은 `equals` 규약 비슷한점
  - 충족 사항
    - 반사성
    - 대칭성
    - 추이성
  - 주의 사항
    - 객체 지향적 추상화의 이점을 포기하지 않으면, 기존 클래스를 확장한 구체 클래스에서 새로운 값 컴포넌트 추가시 -> `compareTo`규약을 못 지킴
      - 우회 방법 -> 확장 대신 독립된 클래스를 만들어 해당 클래스와 원래 클래스의 인스턴스를 가리키는 필드를 활용하여 내부 인스턴스를 반환하는 `view` 메서드 
제공하면 됨
- `compareTo` 메서드는 필드
  - 동치인지 비교 X
  - 순서 비교 O
- 객체 참조 필드 비교를 하려면 `compareTo` 메서드를 재귀적으로 호출
- `Comparable`을 구현하지 않은 필드나 표준이 아닌 순서 비교 필요시 -> `Comparator` 사용

##### 객체 참조가 하나뿐인 비교자
- `Compareable` 일반적인 패턴 -> 참조끼리 비교할 수 있음
```java
public final class CaseInsensitiveString implements Comparable<CaseInsensitiveString> {
    // ...
    public int compareTo(CaseInsensitiveString cis) {
        return String.CASE_INSENSITIVE_ORDER.compare(s, cis.s);
    }
    // ...
}
```
## 자바 7부터 바뀐 사항
- 이전
  - `compareTo` 메서드에서 정수 기본 타입 필드 비교시(관계 연산자`<`, `>`는 실수 기본 타입 필드를 비교) -> `Double.compare` 또는 
`Float.compare` 사용 권장
- 이후
  - 박싱된 기본 타입 클래스에 새로 추가된 정적 메서드인 `compareTo` 이용 권장
  - `compareTo` 메서드에서 관계 연산자(`<`, `>`)를 사용하는 이전 방식은 오류 유발

## 클래스의 핵심 필드가 여러 개일시 비교 순서
- 핵심 필드 순으로 비교 진행
```java
public int compareTo(PhoneNumber pn) {
    int result = Short.compare(areaCode, pn.areaCode); // 가장 중요한 필두
    if (result == 0) {
        result = Short.compare(prefix, pn.prefix); // 두번째 중요한 필드
        if (result == 0)
            result = Short.compareTo(lineNum, pn.lineNum); // 세번째 중요한 필드
    }
    return result;
}
```

## 자바 8부터
- 비교자 생성 메서드와 팀을 꾸려 메서드 연쇄 방식으로 비교자 생성 가능
- 클래스 초기화시 비교자 생성 메서드 2개를 이용해 비교자 생성
- `thenComparingInt`
  - 자바의 추론 능력 덕분에 타입 명시 필요 없음
  - `short` 비교 -> `comparingInt` 사용
  - `float` 비교 -> `comparingDouble` 사용
```java
private static final Comparator<PhoneNumber> COMPARATOR = 
        comparingInt((PhoneNumber pn) -> pn.areaCode)
            .thenComparingInt(pn -> pn.prefix)
            .thenComparingInt(pn -> pn.lineNum);

public int compareTo(PhoneNumber pn) {
    return COMPARATOR.compare(this, pn);
        }
```

##### 해시코드 값의 차를 기준으로 하는 비교자 - 추이성 위배
- 정수 오버플로
- 부동소수점 계산 방식에 따른 오류
```java
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    public int compare(Object o1, Object o2) {
        return o1.hashCode() - o2.hashCode();
    }
}
```

- 아래의 두 가지 방법을 활용 권장
##### compare 메서드를 활용한 비교자
```java
static Comparator<Object> hashCodeOrder = new Comparator<>()  {
    public int compare(Object o1, Object o2) {
        return Integer.compare(o1.hashCode(), o2.hashCode());
    }
}
```

##### 비교자 생성 메서드를 활용한 비교자
```java
static Comparator<Object> hashCodeOrder =
    Comparator.comparingInt(o -> o.hashCode());
```