# equals를 재정의하려거든 hashCode도 재정의하라.
- `equals`를 재정의한 클래스에 모두 `hashCode`를 재정의해야 한다. 그렇지 않으면 `HashMap` 이나 `HashSet` 같은 컬렉션의 원소를 사용될때 문제가 
발생한다.

### Object 명세서에서 발췌한 규약
- `equals` 비교에 사용되는 정보가 변경되지 않았따면, 애플리케이션이 실행되는 동안 그 객체의 `hashCode` 메서드는 일관되게 항상 같은 값을 반환해야 한다. 단,
애플리케이션을 다시 실행한다면 이 값이 달려져도 상관없다.
- `equals`가 두 객체가 같다고 판단했다면, 두 객체의 `hashCode`는 똑같은 값을 반환해야 한다.
- `equals`가 두 객체를 다르게 판단했더라도, 두 객체의 `hashCode`가 서로 다른 값을 반환할 필요는 없다. 단, 다른 값을 반환해야 해시테이블의 성능이 좋아진다.

```java
public static final class PhoneNumber {
        private final short areaCode, prefix, lineNum;

        public PhoneNumber(int areaCode, int prefix, int lineNum) {
            this.areaCode = rangeCheck(areaCode, 999, "지역코드");
            this.prefix = rangeCheck(prefix, 999, "프리픽스");
            this.lineNum = rangeCheck(lineNum, 9999, "가입자 번호");
        }

        private static short rangeCheck(int val, int max, String arg) {
            if (val < 0 || val > max)
                throw new IllegalArgumentException(arg + ": " + val);
            return (short) val;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof PhoneNumber))
                return false;
            PhoneNumber pn = (PhoneNumber) o;
            return pn.lineNum == lineNum && pn.prefix == prefix && pn.areaCode == areaCode;
        }
}
```
```java
@Test
void test(){
        Map<PhoneNumber, String> m=new HashMap<>();
        m.put(new PhoneNumber(707,867,5309),"제니");

        System.out.println(m.get(new PhoneNumber(707,867,5309)));
}
```
출력
```
null
```
- `hashCode`를 재정의하지 않았기 때문에 찾을 수 없다.
  - 엉뚱한 해시 버킷에 가서 객체를 찾으려 시도한다.
  - 같은 버킷에 있더라도 해시코드가 다른 에트리끼리는 동치성 비교 시도를 하지 않는다.

#### 최악의 `hashCode` 구현
```java
@Override
public int hashCode() {
    return 42;
}
```
- 모든 객체가 하나의 해시테이블 버킷에 담겨 `LinkedList`처럼 동작한다. O(1) -> O(n)

## 좋은 hashCode를 작성하는 간단한 요령
1. int 변수 result를 선언한 후 값 c로 초기화한다. 이때 c는 해당 객체의 첫번째 핵심플드를 단계 2.i. 방식으로 계산한 해시코드다.
2. 해당 객체의 나머지 핵심필드 f 각각에 대해 다음 작업을 수행한다.
   1. 해당 필드의 해시코드 c를 계산한다.
      1. 기본 타입 필드에 대해서는 `Type.hashCode(f)` 수행한다. 여기서 `Type`은 해당 기본 타입의 박싱 클래스다.
      2. 참조 타입 필드라면서 이 클래스의 `equals` 메서드가 이 필드의 `equals`를 재귀적으로 호출해 비교한다면, 이 필드의 `hashCode`를 재귀적으로
호출한다. 계산이 더 복잡해질 것 같으면, 이 필드의 표준형을 만들어 그 표준형의 `hashCode`를 호출한다. 필드의 값이 `null`이면 `0`을 사용한다.
      3. 필드가 배열이라면, 핵심 원소 각각을 별도 필드처럼 다룬다. 이상의 규칙을 재귀적으로 적용해 각 핵심 원소의 해시코드를 계산한 다음, 단계 2.ii. 방식으로
갱신한다. 배열에 핵심 원소가 하나도 없다면 단순히 상수 0을 사용한다. 모든 원소가 핵심 원소라면 `Arrays.hashCode`를 사용한다.
   2. 단계 2.i.에서 계산한 해시코드 c로 result를 갱신한다. 코드로는 다음과 같다. `result = 31 * result + c;`
3. result를 반환한다.

- `hashCode` 구현 후 단위 테스트를 통해 동치인 인스턴스가 똑같은 해시 코드를 반환하는지 확인하자.
- `equals` 비교에 사용되지 않은 필드는 반드시 제외해야 한다. 그렇지 않으면 `hashCode` 두 번째 규약을 어기게 될 위험이 있다.
- 곱하는 숫자 31인 이유는 홀수이면서 소수이기 때문이다. (최신 VM들은 이런 최적화를 자동으로 한다.)
  - 짝수일 경우 오버플로 발생시 정보를 잃게 된다.
  - 2를 곱할 경우 시프트 연산과 같은 결과를 낸다.

#### 전형적인 `hashCode` 메서드
```java
@Override
public int hashCode() {
    int result = Short.hashCode(areaCode);
    result = 31 * result + Short.hashCode(prefix);
    result = 31 * result + Short.hashCode(lineNum);
    return result;
}
```
- 위 방법은 단순하고 충분히 빠르지만 충돌이 더욱 적은 방법을 꼭 써야 한다면 구아바의 `com.google.common.hash.Hashing`을 참고하자.
  - `Object.hashCode`와 비교했을때 충돌 방지에 약하다. (https://github.com/google/guava/wiki/HashingExplained)

#### 한 줄 짜리 `hashCode` 메서드 - 성능이 살짝 아쉽다.
```java
@Override
public int hashCode() {
    return Objects.hash(lineNum, prefix, areaCode);
}
```
- 클래스가 불변이고 해시코드를 계산하는 비용이 크다면, 매번 새로 계산하기 보다는 캐싱하는 방식을 고려해야 한다.
  - 해시의 키로 사용되지 않는 경우라면 `hashCode`가 처음 불릴 때 계산하는 지연 초기화 전략을 사용해도 된다.

#### 해시코드를 지연 초기화하는 `hashCode` 메서드 - 스레드 안정성까지 고려해야 한다.
```java
private int hashCode; // 자동으로 0으로 초기화 된다.

@Override
public int hashCode() {
    int result = hashCode;
    if (result == 0) {
        result = Short.hashCode(areaCode);
        result = 31 * result + Shrot.hashCode(prefix);
        result = 31 * result + Short.hashCode(lineNum);
        hashCode = result;
    }
    return result;
}
```
- 성능을 높인다고 해시코드를 계산할 때 핵심 필드를 생략해서는 안 된다.
  - Java 2 전의 String은 최대 16개의 문자로 해시코드를 계산했지만 URL 같은 케이스에서 문제가 발생했다.
- `hashCode`가 반환하는 값의 생성 규칙을 API 사용자에게 자세히 공표하지 말자. 그래야 클라이언트가 이 값에 의지하지 않게 되고, 추후에 계산 방식을 바꿀 수 
있다.
