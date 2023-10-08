# 변경 가능성을 최소화하라
## 불변 클래스
- 정의: 인스턴스의 내부 값을 파괴되는 순간까지 수정할 수 없는 클래스
- 예시
  - `String`
  - 기본 타입의 박싱된 클래스들
  - `BigInteger`
  - `BigDecimal`
- 가변 클래스보다
  - 설계하고 구현하고 사용하기 쉬움
  - 오류가 생길 여지가 적음
  - 훨씬 안전함

## 불변 클래스의 다섯 가지 규칙
1. 객체의 상태를 변경하는 메서드(변경자)를 제공하지 않는다.
2. 클래스를 확장할 수 없도록 한다. 
   - 하위 클래스에서 부주의하게 혹은 나쁜 의도로 객체의 상태를 변하게 만드는 사태를 막아준다.
   - 방법
     - 클래스를 `final`로 선언한다.
     - 모든 생성자를 `private` 혹은 `package-private`으로 만들고 `public` 정적 팩토리를 제공한다.
3. 모든 필드를 `final`로 선언한다.
   - 시스템이 강제하는 수단을 이용해 설계자의 의도를 명확히 드러내는 방법이다.
   - 새로 생성된 인스턴스를 동기화 없이 다른 스레드로 건네도 문제없이 동작하게끔 보장하는데도 필요하다.
4. 모든 필드를 `private`으로 선언한다.
   - 필드가 참조하는 가변 객체를 클라이언트에서 직접 접근해 수정하는 일을 막아준다.
   - `public final`로만 선언해도 불변 객체가 되지만, 다음 릴리스에서 내부 표현을 바꾸지 못하므로 권하지 않는다.
5. 자신 외에는 내부의 가변 컴포넌트에 접근할 수 없도록 한다.
   - 클래스에 가변 객체를 참조하는 필드가 하나라도 있다면 클라이언트에서 그 객체의 참조를 얻을 수 없도록 해야 한다.
   - 클라이언트가 제공한 객체 참조를 가르키게 해서는 안 되며, 접근자 메서드가 그 필드를 그대로 반환해서도 안 된다.
   - 생성자, 접근자, readObject 메소드 모두에서 방어적 복사를 수행하라.

## 함수형 프로그래밍(Functional Programming) vs 절차적 혹은 명령형 프로그래밍(Procedural or Imperative Programming)
|           | 함수형 프로그래밍                               | 절차적 혹은 명령형 프로그래밍           |
|-----------|-----------------------------------------|----------------------------|
| 반환        | 피연산자에 함수를 적용해 그결과를 반환하고 피연산자 자체는 그대로 유지 | 메서드에서 피연산자인 자신을 수정해 상태를 변경 |
| 메서드명      | 전치사(plus, minus, ...)                   | 동사(add, subtract, ...)     |
| 불변 영역의 비율 | 높다                                      | 낮다                         |
| 특징        | 단순                                      | 복잡                         |

### 함수형 프로그래밍
```java
public final class ComplexFP {
    private final double re; // 실수부
    private final double im; // 허수부

    public ComplexFP(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public double realPart() { return re; }
    public double imaginaryPart() { return im; }

    /**
     * 더하기
     */
    public ComplexFP plus(ComplexFP c) {
        return new ComplexFP(re + c.re, im + c.im);
    }

    /**
     * 빼기
     */
    public ComplexFP minus(ComplexFP c) {
        return new ComplexFP(re - c.re, im - c.im);
    }

    /**
     * 곱하기
     */
    public ComplexFP times(ComplexFP c) {
        return new ComplexFP(re * c.re - im * c.im,
                re * c.im + im * c.re);
    }

    /**
     * 나누기
     */
    public ComplexFP dividedBy(ComplexFP c) {
        double tmp = c.re * c.re + c.im * c.im;
        return new ComplexFP((re * c.re + im * c.im) / tmp,
                (im * c.re - re * c.im) / tmp);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ComplexFP)) return false;
        ComplexFP c = (ComplexFP) o;

        return Double.compare(c.re, re) == 0
                && Double.compare(c.im, im) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(re) + Double.hashCode(im);
    }

    public String print() {
        return "(" + re + " + " + im + "i)";
    }
}
```
### 절차적 혹은 명령형 프로그래밍
```java
public final class ComplexPP {
    private double re; // 실수부
    private double im; // 허수부

    public ComplexPP(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public double realPart() { return re; }
    public double imaginaryPart() { return im; }

    /**
     * 더하기
     */
    public ComplexPP add(ComplexPP c) {
        re += c.re;
        im += c.im;
        return this;
    }

    /**
     * 빼기
     */
    public ComplexPP subtract(ComplexPP c) {
        re -= c.re;
        im -= c.im;
        return this;
    }

    /**
     * 곱하기
     */
    public ComplexPP times(ComplexPP c) {
        double tmpRe = re * c.re - im * c.im;
        double tmpIm = re * c.im + im * c.re;
        re = tmpRe;
        im = tmpIm;
        return this;
    }

    /**
     * 나누기
     */
    public ComplexPP dividedByPP(ComplexPP c) {
        double tmp = c.re * c.re + c.im * c.im;
        double tmpRe = (re * c.re + im * c.im) / tmp;
        double tmpIm = (im * c.re - re * c.im) / tmp;
        re = tmpRe;
        im = tmpIm;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ComplexFP)) return false;
        ComplexPP c = (ComplexPP) o;

        return Double.compare(c.re, re) == 0
                && Double.compare(c.im, im) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(re) + Double.hashCode(im);
    }

    public String print() {
        return "(" + re + " + " + im + "i)";
    }
}
```
##### 더하기
```java
@Test
void 더하기() {
    ComplexFP cfp1 = new ComplexFP(1, 0);
    ComplexFP cfp2 = new ComplexFP(0, 1);

    ComplexPP cpp1 = new ComplexPP(1, 0);
    ComplexPP cpp2 = new ComplexPP(0, 1);

    System.out.println("값");
    System.out.print("함수형: " + cfp1.print() + " + " + cfp2.print() + " = ");

    ComplexFP cfpResult = cfp1.plus(cfp2); // 함수형 더하기

    System.out.println(cfpResult.print());

    System.out.print("절차적 혹은 명령형: " + cpp1.print() + " + " + cpp2.print() + " = ");

    ComplexPP cppResult = cpp1.add(cpp2); // 절차적 혹은 명령형 더하기

    System.out.println(cppResult.print());

    System.out.println("주소");
    System.out.println("함수형: " + cfp1 + " + " + cfp2 + " = " + cfpResult);
    System.out.println("절차적 혹은 명령형: " + cpp1 + " + " + cpp2 + " = " + cppResult);
}
```
결과
```
값
함수형: (1.0 + 0.0i) + (0.0 + 1.0i) = (1.0 + 1.0i)
절차적 혹은 명령형: (1.0 + 0.0i) + (0.0 + 1.0i) = (1.0 + 1.0i)
주소
함수형: ComplexFP@be100000 + ComplexFP@3ff00000 = ComplexFP@fe000000
절차적 혹은 명령형: ComplexPP@fe000000 + ComplexPP@3ff00000 = ComplexPP@fe000000
```

##### 빼기
```java
@Test
void 빼기() {
    ComplexFP cfp1 = new ComplexFP(1, 0);
    ComplexFP cfp2 = new ComplexFP(0, 1);

    ComplexPP cpp1 = new ComplexPP(1, 0);
    ComplexPP cpp2 = new ComplexPP(0, 1);

    System.out.println("값");
    System.out.print("함수형: " + cfp1.print() + " - " + cfp2.print() + " = ");

    ComplexFP cfpResult = cfp1.minus(cfp2); // 함수형 빼기

    System.out.println(cfpResult.print());

    System.out.print("절차적 혹은 명령형: " + cpp1.print() + " - " + cpp2.print() + " = ");

    ComplexPP cppResult = cpp1.subtract(cpp2); // 절차적 혹은 명령형 빼기

    System.out.println(cppResult.print());

    System.out.println("주소");
    System.out.println("함수형: " + cfp1 + " - " + cfp2 + " = " + cfpResult);
    System.out.println("절차적 혹은 명령형: " + cpp1 + " - " + cpp2 + " = " + cppResult);
}
```
결과
```
값
함수형: (1.0 + 0.0i) - (0.0 + 1.0i) = (1.0 + -1.0i)
절차적 혹은 명령형: (1.0 + 0.0i) - (0.0 + 1.0i) = (1.0 + -1.0i)
주소
함수형: ComplexFP@be100000 - ComplexFP@3ff00000 = ComplexFP@7e000000
절차적 혹은 명령형: ComplexPP@7e000000 - ComplexPP@3ff00000 = ComplexPP@7e000000
```

### 불변 객체는 스레드 안전하여 따로 동기화할 필요 없다.
- 여러 스레드에 사용해도 절대 훼손되지 않는다. 즉, 여러 스래드와 안심하고 공유할 수 있다.
- 클래스를 스레드 안전하게 만드는 가장 쉬운 방법이다.
- 최대한 재활용하기를 권한다.
- 가장 쉬운 재활용 방법은 자주 쓰이는 값들을 상수(`public static final`)로 제공
```java
public static final Complex ZERO = new Complex(0, 0);
public static final Complex ONE = new Complex(1, 0);
public static final Complex I = new Complex(0, 1);
```
### 불변 클래스는 자주 사용되는 인스턴스를 캐싱하여 같은 인스턴스를 중복 생성하지 않게 해주는 정적 팩터리를 제공할 수 있다.
- 여러 클라이언트가 인스턴스를 공유하여 메모리 사용량과 가비지 컬렉션 비용이 줄어든다.
- 예시
  - 박싱된 기본 타입 클래스
  - `BigInteger`
### 불변 객체는 방어적 복사도 필요 없다.
- 복사를 해도 원본과 똑같다.
- `clone` 메서드나 복사 생성자를 제공하지 않는게 좋다.
- **`String` 클래스의 복사 생성자**는 이 사실 잘 이해하지 못한 자바 초창기 때 만들어진것이라 **사용하지 말아야 한다.**
### 불변 객체끼리는 내부 데이터를 공유할 수 있다.
- `BigInteger` **클래스는 값의 부호**(sign)와 **크기**(magnitude)를 따로 표현한다.
  - 부호 = int 변수
  - 크기 = int 배열
  - `negate` 메서드는 부호만 반대인 새로운 `BigInteger`를 생성한다.
    - 크기, 즉 배열을 복사하지 않고 원본 인스턴스와 공유해도 된다.
### 객체를 만들 때 다른 불변 객체들을 구성요소로 사용하면 생기는 이점
- 아무리 복잡해도 불변식을 유지하기 쉽다.
- 좋은 예
  - 맵의 키와 집합(`Set`)의 원소
    - 안에 담긴 값이 바뀌면 불변식이 허물어지는데, 불변 객체를 사용하면 그런 걱정은 하지 않아도 된다.
### 불변 객체는 그 자체로 실패 원자성을 제공한다.
- 실패 원자성이란 메서드에서 예외가 발생하면 호출 전의 유효한 상태로 돌아오는 성질인데 불변 객체의 메서드는 내부 상태를 바꾸지 않아 이 성질을 만족한다.
### 불변 클래스는 값이 다르면 반드시 독립된 객체로 만들어야 된다.
- 값의 가짓수가 많다면 생성하는데 큰 비용을 치러야한다.
- 예시: `BigInteger`의 `flipBit`
  - 비트 하나를 바꾸기 위해 새로운 인스턴스를 생성한다.
- 해결 방법
  - 다단계 연산들을 예측하여 기본 기능으로 제공
    - 클라이언트가 원하는 복잡한 연산들을 예측할 수 있다면 `package-private`의 가변 동반 클래스만으로 충분하다.
    - 그렇지 않다면 클래스를 `public`으로 제공하는 게 최선이다.
### `final` 클래스 선언 방법 외 불변 클래스 생성하는 법
- 모든 생성자를 `private`혹은 `package-private`으로 만들고 `public` 정적 팩터리를 제공한다.
```java
public class Complex {
    private final double re;
    private final double im;
    
    private Complex(double re, doubl im) {
        this.re = re;
        this.im = im;
    }
    
    public static Complex valueOf(double re, double im) {
        return new Complex(re, im);
    }
    // ...
}
```
### `BigInteger`와 `BigDecimal`의 치명적인 오류
- 설계 당시 객체가 `final`이어야 한다는 생각이 널리 퍼지지 않았다.
- 재정의가 허용되게 설계되어 하위 호환성이 지금까지도 발목을 잡고 있다.
- 신뢰할 수 없는 클라이언트로부터 `BigInteger` 같은 객체를 받으면 주의해야한다.
  - 값들이 불변이어야 클래스의 보안을 지킬 수 있다면 객체가 진짜 `BigInteger`인지 확인해야 한다.
  - 신뢰할 수 없다면 방어적 복사를 사용해야 한다.
```java
public static BigInteger safeInstance(BigInteger val) {
    return val.getClass() == BigInteger.class ? val : new BigInteger(val.toByteArray());
}
```
### 모든 필드가 `final`이 아닌 상태로 불변 클래스 구현하는 방법
- 외부에 비치는 값만 변경할 수 없게 한다.
- 계산 비용이 큰 값을 나중에 계산하게해 `final`이 아닌 필드에 캐싱한다.
  - 동일한 조건의 재요청이 오면 캐싱한 값을 바로 반환하면 비용 절감이 된다.
### 클래스는 필요한 경우가 아니라면 불변이어야 한다.
- `getter`가 있다고 무조건 `setter`를 만들지 말자.
- 불변으로 만들 수 없는 클래스는 변결할 수 있는 부분을 최소화하자.
  - 객체가 가질 수 있는 상태가 줄어들면 객체를 예측하기 쉬워지고 오류가 생길 가능성이 줄어든다.
- 생성자는 불변식 설정이 끝난 상태의 객체를 생성해야 한다.
