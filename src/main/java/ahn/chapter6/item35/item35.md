# ordinal 메서드 대신 인스턴스 필드를 사용하라

열거 타입의 상수는 하나의 정숫값에 대응되며 `ordinal()` 메서드를 사용하면 해당 열거 타입이 몇 번째에 위치되어 있는지 알 수 있다.

##### `ordinal()`을 잘못 사용한 예 - 따라 하지 말 것!

```java
public enum Ensemble {
	SOLO, DUET, TRIO, QUARTET, QUINTET,
	SEXTET, SEPTET, OCTET, NONET, DECTET;

	public int numberOfMusicians() { return ordinal() + 1; }
}
```

이 코드는 정상 작동하지만 유지 보수하기 까다로운 코드다. 상수 선언 순서가 바뀌면 `numberOfMusicians()` 메서드가 반환하는 값이 달라지며, 이미 사용 중인 정수와 값이 같은 상수는 추가할 수 없다.

또한, 특정 값을 건너 뛸 수도 없습니다. 예를 들어 1, 2, 4에 대한 상숫값을 부여할 수 없다.

위의 단점들 때문에 Enum의 API 문서에서도 `ordinal()` 메서드는 대부분의 프로그래머가 사용할 일이 없다고 적혀 있다. `ordinal()`은 `EnumSet`과 `EnumMap` 같은 열거 타입 기반의 범용 자료구조에 사용할 목적으로 설계되었다.

## `ordinal()`의 문제 해결 방법

열거 타입 상수에 연결된 값을 `ordinal()` 메서드를 통해 받지 않고, 인스턴스 필드에 해당 값을 저장하면 된다.

```java
public enum Ensemble {
	SOLO(1), DUET(2), TRIO(3), QUARTET(4), QUINTET(5),
	SEXTET(6), SEPTET(7), OCTET(8), DOUBLE_QUARTET(8), 
	NONET(9), DECTET(10), TRIPLE_QUARTET(12);

	private final int numberOfMusicians;
	Ensemble(int size) { this.numberOfMusicians = size; }
	public int numberOfMusicians() { return numberOfMusicians; }
}
```
