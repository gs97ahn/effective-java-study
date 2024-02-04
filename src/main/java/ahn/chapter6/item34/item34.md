# int 상수 대신 열거 타입을 사용하라

열거 타입은 정의한 값 외의 값을 허용하지 않는 타입이다.

## 정수 열거 패턴(int enum pattern)

##### 상당히 취약하다!

```java
public static final int APPLE_FUJI = 0;
public static final int APPLE_PIPPIN = 1;
public static final int APPLE_GRANNY_SMITH = 2;

public static final int ORANGE_NAVEL = 0;
public static final int ORANGE_TEMPLE = 1;
public static final int ORANGE_BLOOD = 2;
```

### 정수 열거 패턴의 단점

정수 열거 패턴은 타입 안전을 보장할 방법이 없고 표현력도 좋지 않다. 정수의 값이 같다면 서로 다른 타입을 구별할 수 없으며 경고 마저 출력되지 않는다.

정수 열거 패턴을 사용하는 클라이언트의 프로그램을 컴파일하면 상수 값이 파일에 새겨지게 된다. 그래서 상수 값이 바뀌게 되면 클라이언트 프로그램을 다시 컴파일 해야되므로 불필요한 의존성을 가지고 있다.

정수 상수는 문자열을 출력하기 어렵다. 디버깅을 해도 상수 값만 확인할 수 있고, 상수의 개수가 몇개인지 알아내고 싶어도 방법이 없다.

## 문자열 열거(string enum pattern) 패턴

```java
public static final String APPLE_FUJI = "Fuji";
public static final String APPLE_PIPPIN = "Pippin;
public static final String APPLE_GRANNY_SMITH = "Granny Smith";

public static final String ORANGE_NAVEL = "Navel";
public static final String ORANGE_TEMPLE = "Temple";
public static final String ORANGE_BLOOD = "Blood";
```

정수 대신 문자열 상수를 사용하는 변형 패턴으로 문자열 열거 패턴이다. 그러나 문자열 열거 패턴은 더욱 나쁜 결과를 초례하게 된다. 정수 상수와는 다르게 상수의 의미를 출력할 수 있다. 하지만 문자열 상수의 이름 대신 문자열 값을 그대로 하드코딩하기 때문에 오타가 있어도 컴파일러가 오류를 인지하지 못하고 런타임 버그가 생기게 된다. 그리고 문자열 비교로 인해 성능 저하도 발생하게 된다.

## 열거 타입(enum type)

##### 가장 단순한 열거 타입

```java
public enum Apple { FUJI, PIPPIN, GRANNY_SMITH }
public enum Orange { NAVEL, TEMPLE, BLOOD }
```

자바에 열거 타입은 C, C++ 등의 열거 타입과는 다르게 완전한 형태의 클라스라서 단순한 정숫값일 뿐인 다른 언어의 열거 타입보다 강력하다.

자바 열거 타입 자체는 클래스이며 상수 하나당 자신의 인스턴스를 하나씩 만들어 `public static final`로 공개한다. 그리고 밖에 접근할 수 있는 생성자를 제공하지 않기에 사실상 `final`입니다. 즉, 클라이언트가 인스턴스를 생성하거나 확장 할 수 없다. 인스턴스들은 하나씩만 존재하게 되어서 인스턴스 통제된다. 그래서 열거 타입은 싱글턴을 일반화한 형태라고 할 수 있다.

## 컴파일타임 타입 안정성

열거 타입은 컴파일타임 타입 안정성을 제공한다. Apple 열거 타입을 매개변수로 받는 메서드가 있는데 열거 타입 값 중 하나가 아니라면 오류가 발생하게 된다. 사실상 열거 타입의 값끼리 == 연산자로 비교하는것과 같다고 볼 수 있다.

```java
public enum Apple {  
    FUJI, PIPPIN, GRANNY_SMITH;  
  
    public static void print(Apple apple) {  
        System.out.println(apple);  
    }  
}
```

```java
public class AppleTest {  
  
    @Test  
    void test() {  
        Apple.print(Apple.NONE);  
    }  
}
```

```
error: cannot find symbol
        Apple.print(Apple.NONE);
                         ^
  symbol:   variable NONE
  location: class Apple
```

열거 타입에는 각자의 이름공간이 존재해 이름이 같은 상수도 공존할 수 있다. 이는 상수 값이 클라이언트로 컴파일되어 각인되지 않기 때문이다.

```java
public enum ColorOne { RED, GREEN, BLUE }
public enum ColorTwo { RED, GREEN, BLUE }
```

그리고 `toString()` 메서드로 문자열을 출력할 수 있다.

```java
public class AppleTest {  
  
    @Test  
    void toStringTest() {  
        System.out.println(Apple.FUJI.toString());  
    }  
}
```

```
FUJI
```

열거 타입에는 임의의 메서드나 필드를 추가할 수 있으며 임의의 인터페이스도 구현할 수 있다. `Object` 메서드들이 높은 품질로 구현되어 있고 `Comparable`과 `Serializable`이 구현되어 있어 직렬화 형태도 어느정도의 변형을 가해도 문제없이 동작한다.

## 열거 타입에 메서드나 필드 추가

태양계의 여덟 행성을 열거 타입으로 정의하여 메서드나 필드를 추가하는 예를 들겠다. 여덟 행성은 두 속성이 있고, 이를 통해 표면중력을 계산할 수 있다. 이러한 계산과 속성 정리는 열거 타입을 통해 할 수 있다.

##### 데이터와 메서드를 갖는 열거 타입

```java
public enum Planet {  
	MERCURY (3.302e+23, 2.439e6),  
	VENUS   (4.869e+24, 6.052e6),  
	EARTH   (5.975e+24, 6.378e6),  
	MARS    (6.419e+23, 3.393e6),  
	JUPITER (1.899e+27, 7.149e7),  
	SATURN  (5.685e+26, 6.027e7),  
	URANUS  (8.683e+25, 2.556e7),  
	NEPTUNE (1.024e+26, 2.477e7);
  
    private final double mass; // 질량(단위: 킬로그램)  
    private final double radius; // 반지름(단위: 미터)  
    private final double surfaceGravity; // 표면중력 (단위: m/s^2)  
  
    // 중력상수(단위 m^3 / kg s^2)    
    private static final double G = 6.67300E-11;  
  
    // 생성자  
    Planet(double mass, double radius) {  
        this.mass = mass;  
        this.radius = radius;  
        surfaceGravity = G * mass / (radius * radius);  
    }  
  
    public double mass() { return mass; }  
    public double radius() { return radius; }  
    public double surfaceGravity() { return surfaceGravity; }  
  
    public double surfaceWeight(double mass) {  
        return mass * surfaceGravity; // F = ma  
    }  
}
```

```java
public class PlanetTest {  
  
    @Test  
    void test() {  
        double earthWeight = 80.0;  
        double mass = earthWeight / Planet.EARTH.surfaceGravity();  
        for (Planet p : Planet.values())  
            System.out.printf("%s에서의 무게는 %f이다.%n", p, p.surfaceWeight((mass)));  
    }  
}
```

```
MERCURY에서의 무게는 30.232536이다.
VENUS에서의 무게는 72.404081이다.
EARTH에서의 무게는 80.000000이다.
MARS에서의 무게는 30.368320이다.
JUPITER에서의 무게는 202.374355이다.
SATURN에서의 무게는 85.241129이다.
URANUS에서의 무게는 72.388439이다.
NEPTUNE에서의 무게는 90.901082이다.
```

열거 타입 상수 각각을 특정 데이터와 연결지으려면 생성자에서 데이터를 받아 인스턴스 필드에 저장하면 된다. 열거 타입 필드들은 불변이라 `final` 형태로 존재하며, 필드들을 `public`으로 선언해도 되지만, `private`으로 선언하고 `public` 접근자 메서드를 제공하는게 좋다. 그리고 열거 타입은 정의된 상수들의 값을 배열에 담아 반환하는 정적 메서드 `values()`도 기본적으로 제공한다.

## 기존 상수 하나를 제거할 시 발생하는 일

만약 클라이언트 프로그램에서 제거된 상수를 참조하지 않으면 아무런 일이 일어나지 않는다. 하지만 제거된 상수를 참조하고 있는데 클라이언트 프로그램을 다시 컴파일 하면 컴파일 오류가 발생하게 된다. 그리고 만약 다시 컴파일 하지 않으면 예외가 발생하고 의미 있는 오류도 출력해 준다.

## 열거 타입의 접근자

일반 클래스와 동일하게 열거 타입을 선언한 클래스나 패키지에 유용한 기능은 `private`이나 `package-private`으로 구현한다. 이를 통해 클라이언트 입장에서 불필요한 기능들에 대한 노출을 줄일 수 있다.

## 톱레벨 클래스 vs 멤버 클래스

널리 쓰이는 열거 타입은 톱레벨 클래스로 만들고, 특정 톱레벨 클래스에서만 사용된다면 해당 클래스의 멤버 클래로 만들어야 된다. 예를 들어 소수 자릿수의 반올림 모드를 뜻하는 열거 타입 `java.math.RoundingMode`는 `BigDecimal`에서 사용된다. 자바 라이브러리 설계자는 반올림 모드가 `BigDecimal` 외 다른 곳에서도 유용하게 사용될 수 있을거라 판단하여 톱레벨 클래스로 올렸다.

## 더욱 다양한 기능을 제공하는 방법

이전에 `Planet` 열거 타입은 다양한 기능을 제공하고 있다. 하지만 열거 타입에 더 다양한 기능이 필요로 할때가 있다. 사칙 연산을 하는 `Operation` 열거 타입이 직접 연산까지 수행한다는 예를 들겠다.

### `switch`문

##### 값에 따라 분기하는 열거 타입 - 이대로 만족하는가?

```java
public enum Operation {
	PLUS, MINUS, TIMES, DIVIDE;

	public double apply(double x, double y) {
		swtich(this) {
			case PLUS: return x + y;
			case MINUS: return x - y;
			case TIMES: return x * y;
			case DIVIDE: return x / y;
		}
		throw new AssertionError("알 수 없는 연산: " + this);
	}
}
```

`switch`문으로 구현을 하였을때, 코드가 문제 없이 동작하지만 그닥 만족할 수 없는 코드다. 먼저 `throw`문은 절대 도달할 수 없지만 기술적으로는 도달할 수 있기 때문에 생략할 수 없다. 생략을 하게 되면 컴파일 조차 되지 않기 때문이다. 그리고 새로운 상수가 추가될때 마다 관련된 `case`문도 추가해야 되는 번거로움도 있다. 만약 `case`문을 추가하지 않는다면 `throw`문이 동작하게 되고 런타임 오류를 발생하게 된다.

### 상수별 메서드 구현(constant-specific method implementation)

`switch`문을 이용하는거 보다 상수별로 다르게 동작하는 코드를 구현하는 방법이 더 좋다. 열거 타입에 `apply()`라는 추상 메서드를 선언하고 각 상수별 클래스 몸체(constant-specific class body)에서 자신에 맞게 재정의 하는 방법이다. 이 방법은 상수별 메서드 구현이라한다.

##### 상수별 메서드 구현을 활용한 열거 타입

```java
public enum Operation {
	PLUS { public double apply(double x, double y) { return x + y; }},
	MINUS { public double apply(double x, double y) { return x - y; }},
	TIMES { public double apply(double x, double y) { return x * y; }},
	DIVIDE { public double apply(double x, double y) { return x / y; }};

	public abstract double apply(double x, double y);
}
```

상수별 메서드 구현을 활용하면 새로운 상수를 추가할 때마다 `apply()` 메서드도 재정의해야 된다는 사실을 잊기 어려울 것이다. 그리고 재정의를 하지 않았더라도 `apply()` 메서드가 추상 메서드이기 때문에 컴파일 시 오류가 발생할 것이다.

더 나아가 상수별 메서드 구현을 상수별 데이터와 결합할 수도 있다. 그래서 `toString()`를 호출 시 해당 연산을 뜻하는 기호를 반환하도록 할 수 있다.

##### 상수별 클래스 몸체(class body)와 데이터를 사용한 열거 타입

```java
public enum Operation {
	PLUS("+") {
		public double apply(double x, double y) { return x + y; }
	},
	MINUS("-") {
		public double apply(double x, double y) { return x - y; }
	},
	TIMES("*") {
		public double apply(double x, double y) { return x * y; }
	},
	DIVIDE("/") {
		public double apply(double x, double y) { return x / y; }
	};

	private final String symbol;

	Operation(String symbol) { this.symbol = symbol; }

	@Override public String toString() { return symbol; }
	public abstract double apply(double x, double y);
}
```

```java
public class OperationTest {  
  
    @Test  
    void toStringTest() {  
        // given  
        double x = 2.0;  
        double y = 4.0;  
  
        // when & then  
        for (Operation op : Operation.values())  
            System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x, y));  
    }  
}
```

```
2.000000 + 4.000000 = 6.000000
2.000000 - 4.000000 = -2.000000
2.000000 * 4.000000 = 8.000000
2.000000 / 4.000000 = 0.500000
```

마지막으로 `toString()` 메서드를 재정의하려거든, 문자열을 해당 열거 타입 상수로 변환해 주는 `fromString()` 메서드를 제공하는 걸 고려해 봐야 된다.

##### 열거 타입 `fromString()` 메서드 구현하기

```java
public enum Operation {
	// ...
	private static final Map<String, Operation> stringToEnum = Stream.of(values())  
        .collect(toMap(Objects::toString, e -> e));

	// 지정한 문자열에 해당하는 Operation을 (존재한다면) 반환한다.  
	public static Optional<Operation> fromString(String symbol) {  
	    return Optional.ofNullable(stringToEnum.get(symbol));  
	}
}
```

```java
public class OperationTest {
  
    @Test  
    void fromStringTest() {  
        // given  
        String symbol = "+";  
  
        // when  
        Operation operation = Operation.fromString(symbol)  
                .orElseThrow(() -> {  
                    throw new AssertionError("알 수 없는 연산");  
                });  
  
        // then  
        System.out.println(operation);  
    }  
}
```

```
+
```

열거 타입 상수가 생성된 후 정적 필드가 초기화 될 때 `stringToEnum` 맵에 `Operation` 상수가 추가된다.

## 상수별 메서드 구현의 단점

상수별 메서드 구현에는 열거 타입 상수끼리 코드를 공유하기 어렵다.

우선 급여명세서 열거 타입을 예시를 보겠다. 이 열거 타입은 직원의 기본 임금, 주중에 오버타임 발생시 주어지는 잔업 수당, 주말에 근무를 할 경우 잔업수당까지 고려하여 임금 계산을 한다.

##### 값에 따라 분기하여 코드를 공유하는 열거 타입 - 좋은 방법인가?

```java
enum PayrollDay {
	MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

	private static final int MINS_PER_SHIFT = 8 * 60;

	int pay(int minutesWorked, int payRate) {
		int basePay = minutesWorked * payRate;

		int overtimePay;

		switch(this) {
			case SATURDAY: case SUNDAY: // 주말
				overtimePay = basePay / 2;
				break;
			default: // 주중
				overtimePay = minutesWorked <= MINS_PER_SHIFT ?
					0 : (minutesWorked - MINS_PER_SHIFT) * payRate / 2;
		}

		return basePay + overtimePay;
	}
}
```

위에 `Operation` 열거 타입과 동일하게 해당 열거타입은 관리하기 어려운 코드다. 휴가 같은 새로운 케이스가 생겨 열거 타입에 추가하면 관련 `case`도 `switch`문에 작성 해주어야 되기 때문이다.

상수별 메서드 구현을 활용하여 정확히 급여 계산을 하는 두 가지 방법이 있다.

1. 잔업수당을 계산하는 코드를 모든 상수에 중복해서 넣는다.
2. 계산 코드를 평일용과 주말용으로 나눠 각가을 도우미 메서드로 작성한 다음 각 상수가 자신에게 필요한 메서드를 적절히 호출한다.

하지만 두 방식 모두 코드가 장황해지고 가독성이 떨어져 오류 발생 가능성을 높인다.

가장 좋은 방법은 새로운 상수가 추가될 때 마다 잔업수당 전략을 선택하도록 하는 것이다. 이는 수당 계산을 중첩 열거 타입인 `PayType`으로 위임하면 가능하다.

##### 전략 열거 타입 패턴

```java
enum PayrollDay {
	MONDAY(WEEKDAY), TUESDAY(WEEKDAY), WEDNESDAY(WEEKDAY),
	THURSDAY(WEEKDAY), FRIDAY(WEEKDAY),
	SATURDAY(WEEKEND), SUNDAY(WEEKEND);

	private final PayType payType;

	PayrollDay(PayType payType) { this.payType = payType; }

	int pay(int minutesWorked, int payRate) { 
		return payType.pay(minutesWorked, payRate);
	}

	// 전략 열거 타입
	enum PayType {
		WEEKDAY {
			int overtimePay(int minsWorked, int payRate) {
				return minsWorked <= MINS_PER_SHIFT ? 
					0 : (minsWorked - MINS_PER_SHIFT) * payRate / 2;
			}
		},
		WEEKEND {
			int overtimePay(int minsWorked, int payRate) {
				return minsWorked * payRate / 2;
			}
		};

		abstract int overtimePay(int mins, int payRate);
		private static final int MINS_PER_SHIFT = 8 * 60;

		int pay(int minsWorked, int payRate) {
			int basePay = minsWorked * payRate;
			return basePay + overtimePay(minsWorked, payRate);
		}
	}
}
```

대부분의 경우 `switch`문을 사용하는건 적합하지 않는다. 하지만 기존 열거 타입에 상수별 동작을 혼합해 넣을 경우 `switch`문은 좋은 선택이 될 수 있다. 예를 들어 기존 `Operation` 열거 타입에 반대 연산을 반환하는 메서드가 필요하다고 해보겠다.

##### `switch`문을 이용해 원래 열거 타입에 없는 기능을 수행한다

```java
public static Operation inverse(Operation op) {
	switch(op) {
		case PLUS: return Operation.MINUS;
		case MINUS: return Operation.PLUS;
		case TIMES: return Operation.DIVIDE;
		case DIVIDE: return Operation.TIMES;

		default: throw new AssertionError("알 수 없는 연산: " + op);
	}
}
```

종합적으로 종류와 수를 컴파일타임에 파악할 수 있는 상수 집합이라면 열거 타입을 사용하는 게 좋다. 물론 열거 타입에 정의된 상수 개수는 영원히 고정 불변일 필요는 없다. 그 이유는 열거 타입은 나중에 추가되어도 바이너리 수준에서 호환되도록 설계되었기 때문이다.

## 정리

정수 열거 패턴 또는 문자열 열거 패턴같이 상수 집합의 정의가 필요할 땐 열거 타입을 사용하는 게 좋다. 그 이유는 열거 타입은 읽기 쉽고, 안전하고, 강력하기 때문이다. 열거 타입은 명시적 생성자나 메서드 없이도 사용할 수 있지만, 각 상수를 특정 데이터와 연결 짓거나 상수마다 다르게 동작하게 만들어야 될 때 메서드나 필드를 추가하면 된다.