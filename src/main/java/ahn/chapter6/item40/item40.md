# @Override 애너테이션을 일관되게 사용하라

`@Override` 애너테이션은 상위 타입의 메서드를 재정의 했다는 뜻을 가지고 있고 메서드 선언에만 달 수 있다. `@Override`는 악명 높은 버그들을 예방해 준다. 그래서 자바가 기본으로 제공하는 애너테이션 중 프로그래머에게 가장 중요한 애너테이션이다.

##### 영어 알파벳 2개로 구성된 문자열을 표현하는 클래스 - 버그를 찾아보자.

```java
public class Bigram {  
  
    private final char first;  
    private final char second;  
  
    public Bigram(char first, char second) {  
        this.first = first;  
        this.second = second;  
    }  
  
    public boolean equals(Bigram b) {  
        return b.first == first && b.second == second;  
    }  
  
    public int hashCode() {  
        return 31 * first + second;  
    }  
}
```

```java
public class BigramTest {  
  
    @Test  
    void test() {  
        // given  
        Set<Bigram> s = new HashSet<>();  
  
        // when  
        for (int i = 0; i < 10; i++)  
            for (char ch = 'a'; ch <= 'z'; ch++)  
                s.add(new Bigram(ch, ch));  
  
        // then  
        System.out.println(s.size());  
    }  
}
```

```
260
```

해당 코드에는 a 부터 z까지 10번을 반복하여 `Set<Bigram>`에 원소들이 추가된다. `HashSet`은 중복 원소를 함께 담을 수 없기 때문에 출력값이 26일것이라 예상하지만 260이 출력된 것을 확인할 수 있다.

이유는 `equals()` 메서드를 올바르게 재정의 하지 않았기 때문이다. `euqals()`를 재정하기 위해서는 매개변수로 `Object`를 받아야 되지만, 현재 `equals()`는 `Bigram`을 매개변수로 받고 있기 때문에 다중정의(overloading)한 샘이 된다.

우선 재정의를 위해 `@Override` 애너테이션을 선언해보자.

```java
public class Bigram {  
  
    // ...
  
	@Override
    public boolean equals(Bigram b) {  
        return b.first == first && b.second == second;  
    }  
  
    // ...
}
```

```
java: method does not override or implement a method from a supertype
```

`@Override` 애너테이션만 달고 매개변수는 수정하지 않았기 때문에 컴파일러가 해당 에러 메세지를 던진다. 올바른 동작을 위해서는 다음과 같이 코드를 수정해 주면 된다.

```java
public class Bigram {  
  
    // ...
  
	@Override
	public boolean equals(Object o) {
	    if (!(o instanceof Bigram))
	        return false;
	    Bigram b = (Bigram) o;
	    return b.first == first && b.second == second;
	}
  
    // ...
}
```

```java
public class BigramTest {  
  
    @Test  
    void test() {  
        // given  
        Set<Bigram> s = new HashSet<>();  
  
        // when  
        for (int i = 0; i < 10; i++)  
            for (char ch = 'a'; ch <= 'z'; ch++)  
                s.add(new Bigram(ch, ch));  
  
        // then  
        System.out.println(s.size());  
    }  
}
```

```
26
```

## `@Override`에 적절한 사용법

위에 `Bigram`에서 `@Override` 애너테이션을 `equals()` 메서드에 선언한 것처럼, 상위 클래스 메서드를 재정의 하려는 모든 메서드에 `@Override` 애너테이션을 달면 된다. `@Override` 애너테이션을 통해 해당 메서드가 재정의 되었다는 사실을 알 수 있고, 만약 재정의가 제대로 되지 않았다면 컴파일시 확인할 수도 있기 때문이다.

단, 구체 클래스에서 상위 클래스의 추상 메서드를 재정의할 때는 굳이 `@Override`를 달지 않아도 된다. 왜냐면 추상 메서드를 아직 구현하지 않은 구체 클래스가 있다면 컴파일러가 그 사실을 알려주기 때문이다. 하지만 `@Override` 애너테이션을 해당 메서드에 달아도 특별히 문제가 되진 않는다.

그리고 클래스 말고도 인터페이스의 메서드를 재정의할 때도 `@Override`를 사용할 수 있다. 인터페이스를 구현한 클래스에서 인터페이스에 있는 모든 메서드들을 전부 구현했는지에 대한 확인을 할 수 있기 때문이다.

## 정리

상위 클래스 메서드를 재정의한 모든 하위 메서드에는 `@Override`를 달자. 하지만 구체 클래스에서 상위 클래스의 추상 메서드를 재정의한다면 `@Override`를 달지 않아도 된다.