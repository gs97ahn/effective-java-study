# 확장할 수 있는 열거 타입이 필요하면 인터페이스를 사용하라

대부분의 경우 열거 타입은 타입 안전 열거 패턴(typesafe enum pattern)보다 좋지만 한 가지 단점이 있다. 타입 안전 열거 패턴은 확장에 열려 있어 열거 값들을 추가 할 수 있는 반면 열거 타입은 확장에 닫혀있다.

## 열거 타입 확장

열거 타입을 확장하는건 대부분 좋지 않은 생각이다. 확장한 타입의 원소는 기반 타입의 원소를 취급하지만 그 반대는 성립하지 않고 기반 타입과 확장된 타입의 원소들을 모두 순회할 방법도 없기 때문이다.

### 확장할 수 있는 열거 타입

연산 코드(operation code 혹은 opcode)만 확장할 수 있는 열거 타입에 어울린다. 연산 코드의 각 원소는 특정 기계가 수행하는 연산을 뜻한다.

확장 할 수 있는 열거 타입을 만들고 싶으면 인터페이스를 만들어서 열거 타입이 해당 인터페이스를 구현하게 하면 된다. 자바 라이브러리에서 `java.nio.file.LinkOption` 열거 타입은 `CopyOption`과 `OpenOption` 인터페이스가 구현 되어 있다.

##### 인터페이스를 이용해 확장 기능 열거 타입을 흉내 냈다.

```java
public interface Operation {
	double apply(double x, double y);
}
```

```java
public enum BasicOperation implements Operation {
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

	BasicOperation(String symbol) {
		this.symbol = symbol;
	}

	@Override public String toString() {
		return symbol;
	}
}
```

열거 타입인 `BasicOperation`은 확장할 수 없지만 인터페이스인 `Operation`은 확장할 수 있다. 그리하여 타입을 확장하고 싶으면 다른 열거 타입으로 `Operation`을 구현하면 된다.

##### 확장 가능 열거 타입

```java
public enum ExtendedOperation implements Operation {
	EXP("^") {
		public double apply(double x, double y) {
			return Math.pow(x, y);
		}
	},
	REMAINDER("%") {
		public double apply(double x, double y) {
			return x % y;
		}
	};

	private final String symbol;

	ExtendedOperation(String symbol) {
		this.symbol = symbol;
	}

	@Override public String toString() {
		return symbol;
	}
}
```

## 확장된 열거 타입 사용 방법

첫 번째 방법은 한정적 타입 토큰인 `class` 리터럴을 활용하면 확장된 열거 타입을 사용할 수 있다. `<T extends Enum<T> & Operation> Class<T> `로 매개변수를 선언하면 `Class` 객체가 열거 타입이면서 `Operation`의 하위 타입이게 된다.

```java
public class OperationTest {  
  
    @Test  
    void extendedOperationTest() {  
        // given  
        double x = 3.0;  
        double y = 2.0;  
  
        // when & then  
        test(ExtendedOperation.class, x, y);  
    }  
  
    private static <T extends Enum<T> & Operation> void test(Class<T> opEnumType, double x, double y) {  
        for (Operation op : opEnumType.getEnumConstants())  
            System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x, y));  
    }  
}
```

```
3.000000 ^ 2.000000 = 9.000000
3.000000 % 2.000000 = 1.000000
```

두 번째 방법은 한정적 와일드카드 타입인 `Collection<? extends Operation>`을 활용하는 방법이다. 이 방법을 통해 여러 구현 타입의 연산을 조합해 호출할 수 있어 더 유연해지지만, 특정 연산에서 `EnumSet`과 `EnumMap`을 사용하지 못한다.

```java
public class OperationTest {  
  
    @Test  
    void extendedOperationTest2() {  
        // given  
        double x = 3.0;  
        double y = 2.0;  
  
        // when & then  
        test(List.of(ExtendedOperation.values()), x, y);  
    }  
  
    private static void test(Collection<? extends Operation> opSet, double x, double y) {  
        for (Operation op : opSet)  
            System.out.printf("%f %s %f = %f %n", x, op, y, op.apply(x, y));  
    }
}
```

```
3.000000 ^ 2.000000 = 9.000000 
3.000000 % 2.000000 = 1.000000 
```

## 정리

열거 타입 기본적으로 확장할 수 없다. 하지만 인터페이스와 그 인터페이스를 구현하는 기본 열거 타입을 선언하면 열거 타입을 확장하는 효과를 낼 수 있다.