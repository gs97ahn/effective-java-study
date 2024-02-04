# 비트 필드 대신 EnumSet을 사용하라

예전에는 열거한 값이 집합 형태일 경우, 각 상수에 서로 다른 2의 거듭제곱 값을 할당한 정수 열거 패턴을 사용했다.

##### 비트 필드 열거 상수 - 구닥다리 기법!

```java
public class Text {
	public static final int STYLE_BOLD = 1 << 0; // 1
	public static final int STYLE_ITALIC = 1 << 1; // 2
	public static final int STYLE_UNDERLINE = 1 << 2; // 4
	public static final int STYLE_STRIKETHROUGH = 1 << 3; // 8

	// 매개변수 styles는 0개 이상의 STYLE_ 상수를 비트별 OR한 값이다.
	public void applyStyles(int styles) { ... }
}
```

다음과 같은 식으로 비트별 OR을 사용해 여러 상수를 하나의 집합으로 모을 수 있으며, 이러한 집합을 비트 필드(bit field)라 한다.

```java
text.applyStyles(STYLE_BOLD | STYLE_ITALIC);
```

비트 필드를 활용하면 비트별 연산을 통해 합집합, 교집합 같은 집합 연산을 효율적으로 수행할 수 있다. 하지만 정수 열거 상수와 동일한 단점을 가지고 있으며, 다음과 같은 단점을 추가적으로 가지고 있다.

- 비트 필드 값을 출력하면 정수 열거 상수를 출력한거 보다 해석하기 어렵다
- 비트 필드 하나에 녹아 있는 모든 원소를 순회하기 까다롭다
- API 작성시 최대 몇 비트가 필요한지 예측해서 적절한 타입(int, long, ...)을 선택해야 된다
- 비트 수(32비트 or 64비트)를 늘리려면 API를 수정해야 된다

## 대안

`java.util` 패키지에 `EnumSet` 클래스는 열거 타입 상수의 값으로 구성된 집합을 효과적으로 표현해 준다. `EnumSet`은 `Set` 인터페이스를 완벽히 구현하고 있으며, 타입 안정하고, 다른 어떤 `Set` 구현체와도 함께 사용할 수 있다.

또한 `EnumSet`의 내부는 비트 벡터로 구현되어 있다. 원소가 총 64개 이하라면, `EnumSet` 전체를 `long` 변수 하나로 표현하기에 효율적인 성능을 낸다.

##### `EnumSet` - 비트 필드를 대체하는 현대적 기법

```java
public class Text {
	public enum Style { BOLD, ITALIC, UNDERLINE, STRIKETHROUGH }

	// 어떤 Set을 넘겨도 되나, EnumSet이 가장 좋다.
	public void applyStyles(Set<Styles> styles) { 
        // ... 
    }
}
```

`EnumSet`은 집합 생성 등 다양한 기능의 정적 팩터리를 제공하는데, 다음 코드는 그중 `of()` 메서드를 사용한 예시다.

```java
text.applyStyles(EnumSet.of(Style.BOLD, Style.ITALIC));
```

## 정리

집합 형태의 타입을 정의할 때는 비트 필드를 사용하지 말고 `EnumSet` 클래스를 활용하는 게 좋다.