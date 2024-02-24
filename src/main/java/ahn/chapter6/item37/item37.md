# ordinal 인덱싱 대신 EnumMap을 사용하라

배열이나 리스트에서 원소를 꺼낼 때 `ordinal()` 메서드를 통해 인덱스를 얻을 수 있다.

다음의 예시 코드는 생애 주기(한해살이, 여러해살이, 두해살이) 3개를 집합을 만들어 각 집합을 식물에 입력한다. 이때 `ordinal` 값을 배열의 인덱스로 사용한다.

```java
public class Plant {  
    enum LifeCycle { ANNUAL, PERENNIAL, BIENNIAL }  
  
    final String name;  
    final LifeCycle lifeCycle;  
  
    Plant(String name, LifeCycle lifeCycle) {  
        this.name = name;  
        this.lifeCycle = lifeCycle;  
    }  
  
    @Override public String toString() {  
        return name;  
    }  
}
```

##### `ordinal()`을 배열 인덱스로 사용 - 따라 하지 말 것!

```java
public class PlantTest {  
  
    @Test  
    void badExampleTest() {  
        // given  
        Plant[] garden = {
		        new Plant("장미", Plant.LifeCycle.ANNUAL),
		        new Plant("코스모스", Plant.LifeCycle.ANNUAL),
		        new Plant("튤립", Plant.LifeCycle.PERENNIAL),  
		        new Plant("민들레", Plant.LifeCycle.BIENNIAL),  
		        new Plant("국화", Plant.LifeCycle.BIENNIAL)  
		};
  
        // when  
        Set<Plant>[] plantsByLifeCycle = (Set<Plant>[]) new Set[Plant.LifeCycle.values().length];
        for (int i = 0; i < plantsByLifeCycle.length; i++)  
            plantsByLifeCycle[i] = new HashSet<>();
  
        for (Plant p : garden)
            plantsByLifeCycle[p.lifeCycle.ordinal()].add(p);
  
        // then  
        for (int i = 0; i < plantsByLifeCycle.length; i++)  
            System.out.printf("%s: %s%n", Plant.LifeCycle.values()[i], plantsByLifeCycle[i]);  
    }  
}
```

```
ANNUAL: [코스모스, 장미]
PERENNIAL: [튤립]
BIENNIAL: [국화, 민들레]
```
## `ordinal`을 인덱스로 사용하면 위험한 이유

우선 배열이 제네릭과 호환되지 않기 때문에 비검사 형변환을 거치게 되고, 이로 인해 컴파일이 깔끔하게 되지 않는다. 그리고 각 인덱스의 의미를 알 수 없기에 출력 결과에 레이블을 직접 달아야 한다. 또한, `ordinal()` 메서드를 사용해 배열에 원소들을 삽입할 때 정확한 정숫값을 사용한다는 것을 직접 보증해야 한다. 잘못된 값이 입력되면 문제를 알아차리기 힘들거나 `ArrayIndexOutOfBoundsException`이 발생할 수도 있다.

## `ordinal`외 다른 해결책

실질적으로 배열은 상숫값과 열거 타입을 매핑하는 일을 하는것이니 `Map`을 사용해서 이전에 위험 요소를 제거할 수 있다. 그리고 열거 타입을 사용할 경우 열거 타입을 키로 사용하도록 설계한 `Map` 구현체인 `EnumMap`을 사용해야 된다.

##### `EnumMap`을 사용해 데이터와 열거 타입을 매핑한다.

```java
public class PlantTest {  
  
    @Test  
    void goodExampleTest() {  
        // given  
        Plant[] garden = {  
		        new Plant("장미", Plant.LifeCycle.ANNUAL),  
		        new Plant("코스모스", Plant.LifeCycle.ANNUAL),  
		        new Plant("튤립", Plant.LifeCycle.PERENNIAL),  
		        new Plant("민들레", Plant.LifeCycle.BIENNIAL),  
		        new Plant("국화", Plant.LifeCycle.BIENNIAL)  
		};
  
        // when  
        Map<Plant.LifeCycle, Set<Plant>> plantsByLifeCycle = new EnumMap<>(Plant.LifeCycle.class);  
        for (Plant.LifeCycle lc : Plant.LifeCycle.values())  
            plantsByLifeCycle.put(lc, new HashSet<>());  
  
        for (Plant p : garden)  
            plantsByLifeCycle.get(p.lifeCycle).add(p);  
  
        // then  
        System.out.println(plantsByLifeCycle);  
    }  
}
```

```
{ANNUAL=[코스모스, 장미], PERENNIAL=[튤립], BIENNIAL=[민들레, 국화]}
```

이전 코드와 다르게 비검사 형변환을 거치지 않고, 출력용 문자열을 제공하기 때문에 출력 결과에 직접 레이블을 달지 않아도 된다. 그리고 배열 인덱스 계산 과정에서 오류가 날 가능성이 없습니다. 성능상으로는 배열을 썼을 때와 비슷한데, 그 이유는 `EnumMap`도 내부적으로 배열을 사용하기 때문이다.

```java
public class EnumMap<K extends Enum<K>, V> extends AbstractMap<K, V> 
	implements java.io.Serializable, Cloneable 
{
	private final Class<K> keyType; // 열거 타입의 클래스 타입
	private transient K[] keyUniverse; // 맵의 키
	private transient Object[] vals; // 맵의 값
	private transient int size = 0; // 원소의 수
	// ...
}
```

## 코드를 더 줄이는 법

스트림 방식을 사용하면 이전 코드들과 동일한 동작을 하지만 획기적으로 코드의 수를 줄일 수 있다.

##### 스트림을 사용한 코드 1 - `EnumMap`을 사용하지 않는다!

```java
public class PlantTest {  

	@Test  
	void streamExampleTest1() {  
	    // given  
	    Plant[] garden = {  
	            new Plant("장미", Plant.LifeCycle.ANNUAL),  
	            new Plant("코스모스", Plant.LifeCycle.ANNUAL),  
	            new Plant("튤립", Plant.LifeCycle.PERENNIAL),  
	            new Plant("민들레", Plant.LifeCycle.BIENNIAL),  
	            new Plant("국화", Plant.LifeCycle.BIENNIAL)  
	    };  
	  
	    // when & then  
	    System.out.println(Arrays.stream(garden)  
	            .collect(groupingBy(p -> p.lifeCycle)));  
	}
}
```

```
{BIENNIAL=[민들레, 국화], ANNUAL=[장미, 코스모스], PERENNIAL=[튤립]}
```

하지만 이 방법은 `EnumMap`을 사용하지 않기 때문에 `EnumMap`의 공간과 성능 이점이 사라진다. `EnumMap`의 이점을 이용할 수 있도록 `EnumMap`을 이용해 보겠다.

##### 스트림을 사용한 코드 2 - `EnumMap`을 이용해 데이터와 열거 타입을 매핑했다.

```java
public class PlantTest {

	@Test  
	void streamExampleTest2() {  
	    // given  
	    Plant[] garden = {  
	            new Plant("장미", Plant.LifeCycle.ANNUAL),  
	            new Plant("코스모스", Plant.LifeCycle.ANNUAL),  
	            new Plant("튤립", Plant.LifeCycle.PERENNIAL),  
	            new Plant("민들레", Plant.LifeCycle.BIENNIAL),  
	            new Plant("국화", Plant.LifeCycle.BIENNIAL)  
	    };  
	  
	    // when & then  
	    System.out.println(Arrays.stream(garden)  
	            .collect(groupingBy(p -> p.lifeCycle,  
	                    () -> new EnumMap<>(Plant.LifeCycle.class), toSet())));  
	}
}
```

```
{ANNUAL=[장미, 코스모스], PERENNIAL=[튤립], BIENNIAL=[민들레, 국화]}
```

이와 같이 매번 최적화할 필요는 없지만, 맵을 자주 사용하는 프로그램이라면 `EnumMap`을 이용하는 방법이 더 좋을 것이다.

두 스트림 출력 방식의 다르다. 하나의 열거 타입에 대한 데이터가 없을 경우, `EnumMap`을 사용하지 않은 스트림 방식은 맵을 2개만 만드는 반면 `EnumMap`을 사용하면 3개의 맵을 만든다.

## 두 열거 타입 값을 매핑하는 방법

`ordinal`을 이용해 두 열거 타입 값들을 매핑하려면 `ordinal`을 두 번 써야 된다.

다음의 예시 코드는 두 가지 상태(Phase)를 전이(Transition)와 매핑되는 프로그램이다.

##### 배열들의 배열의 인덱스에 `ordinal()`을 사용 - 따라 하지 말 것!

```java
public enum Phase {  
    SOLID, LIQUID, GAS;  
  
    public enum Transition {  
        MELT, FREEZE, BOIL, CONDENSE, SUBLIME, DEPOSIT;  
  
        // 행은 from의 ordinal을, 열은 to의 ordinal을 인덱스로 쓴다.  
        private static final Transition[][] TRANSITIONS = {  
                { null, MELT, SUBLIME },  
                { FREEZE, null, BOIL },  
                { DEPOSIT, CONDENSE, null }  
        };  
  
        // 한 상태에서 다른 상태로 전의를 반환한다.  
        public static Transition from(Phase from, Phase to) {  
            return TRANSITIONS[from.ordinal()][to.ordinal()];  
        }  
    }  
}
```

이전에도 설명했듯이 컴파일러가 `ordinal`과 배열 인덱스 관계를 알 수 없다. 그래서 `Phase`나 `Phase.Transition`을 수정하면 반드시 `TRANSITION`도 함께 수정해 주어야 된다. 그러지 않으면 런타임에 `ArrayIndexOutOfBoundException`이나 `NullPointerException` 같은 오류가 발생할 수 있다. 최악의 경우, 예외조차 던져지지 않고 코드가 이상하게 동작하게 된다. 그리고 상전이 표의 크기는 상태의 가짓수가 늘어나면 제곱해서 커지고 null로 채워지는 칸도 늘어나게 된다.

## `EnumMap`으로 해결

이번에도 `EnumMap`을 두 개를 사용하여, 안쪽 맵은 이전 상태와 전이를 연결하고 바깥 맵은 이후 상태와 안쪽 맵을 연결하면 된다.

```java
public enum Phase {  
    SOLID, LIQUID, GAS;  
  
    public enum Transition {  
        MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID),  
        BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID),  
        SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID);  
  
        private final Phase from;  
        private final Phase to;  
  
        Transition(Phase from, Phase to) {  
            this.from = from;  
            this.to = to;  
        }  
  
        // 상전이 맵을 초기화한다.  
        private static final Map<Phase, Map<Phase, Transition>> m = Stream.of(values())  
                .collect(groupingBy(t -> t.from, () -> new EnumMap<>(Phase.class),  
                        toMap(t -> t.to, t -> t,  
                                (x, y) -> y, () -> new EnumMap<>(Phase.class))));  
  
  
        // 한 상태에서 다른 상태로 전의를 반환한다.  
        public static Transition from(Phase from, Phase to) {  
            return m.get(from).get(to);  
        }  
    }  
}
```

## 열거 타입에 새로운 상수 추가

`Phase` 열거 타입에 새로운 상수 `PLASMA`를 `Phase`에 추가하고 `Phase.Transition`에 `IONIZE`와 `DEIONIZE`를 추가해 보겠다.

먼저 `ordinal`로 구성된 이전 코드에 새로운 상수를 추가하려면 `Phase`에 1개, `Phase.Transition`에 2개를 추가하고 원소 9개짜리인 배열들의 배열을 원소 16개짜리로 교체해야 된다. 만약 원소의 수를 더 많이 또는 더 적게 기입하면, 잘못된 순서로 나열을 하거나 런타임 오류가 발생한다.

반면 `EnumMap`으로 구성된 코드는 새로운 상수들만 잘 해당 열거 타입에 잘 기입해 주기만 하면 된다.

##### `EnumMap` 버전에 새로운 상태 추가하기

```java
public enum Phase {  
    SOLID, LIQUID, GAS, PLASMA;  
  
    public enum Transition {  
        MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID),  
        BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID),  
        SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID),  
        IONIZE(GAS, PLASMA), DEIONIZE(PLASMA, GAS);
        // ...
    }  
}
```

## 정리

열거 타입을 배열로 만들 때 `ordinal`로 배열의 인덱스를 구성하지 말고 `EnumMap`을 활용해야 된다. 그리고 다차원 관계는 `EnumMap<..., EnumMap<...>>`으로 표현해야 오류 또는 오작동을 피할 수 있다.