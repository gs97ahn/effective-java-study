# 적시에 방어적 복사본을 만들라

C, C++과 다르게 Java는 네이티브 메서드를 사용하지 않기 때문에 버퍼 오버런, 배열 오버런등의 메모리 충돌 오류로부터 안전하다. 그리고 클래스가 시스템의 다른 부분에서 무슨 짓을 하든 불변식이 지켜진다. 하지만 다른 클래스로부터의 침범은 막을 수 없기 때문에 방어적 프로그래밍을 해야 한다.

## 객체 내부에서 허락 없이 수정되는 문제

어떤 객체든 그 객체의 허락 없이 외부에서 내부를 수정하게 하면 안된다.

##### 기간을 표현하는 클래스 - 불변식을 지키지 못했다.

```java
public final class Period {

	private final Date start;
	private final Date end;
	
	/**
	 * @param start 시작 시각
	 * @param end 종료 시각; 시작 시각보다 뒤여야 한다.
	 * @throws IllegalArgumentException 시작 시각이 종료 시각보다 늦을 때 발생한다.
	 */
	public Period(Date start, Date end) {
		if (start.compareTo(end) > 0)
			throw new IllegalArgumentException(start + "가 " + end + "보다 늦다.");
		this.start = start;
		this.end = end;
	}
	
	public Date start() {
		return start;
	}
	
	public Date end() {
		return end;
	}
	
	// ...
}
```

```java
public class PeriodTest {  
  
	@Test  
	void constructorTest() {  
	    // given  
	    Date start = new Date();  
	    Date end = new Date();  
	    Period p = new Period(start, end);  
	  
	    System.out.println("수정 전: " + p.end());  
	  
	    // when  
	    end.setYear(78); // p의 내부를 수정했다.  
	  
	    // then    
	    System.out.println("수정 후: " + p.end());  
	}
}
```

```
수정 전: Sat Mar 30 12:10:00 KST 2024
수정 후: Thu Mar 30 12:10:00 KST 1978
```
## 해결 방법

`Date` 대신 불변인 `Instant`, 또는 `LocalDateTime`이나 `ZonedDateTime`을 사용하면 된다. 하지만 `Date`는 오랜 기간 사용 되었기 때문에 쉽게 해방될 수 없다.

### 객체를 생성할 때 복사본을 활용하자

외부 공격으로부터 인스턴스를 안전하게 보호하려면 생성자에서 받은 가변 매개변수 각각을 방어적으로 복사(defensive copy)를 하고 인스턴스 안에서 원본이 아닌 복사본을 사용해야 한다.

```java
public final class Period {
  
    private final Date start;  
    private final Date end;  
  
    public Period(Date start, Date end) {  
        this.start = new Date(start.getTime());  
        this.end = new Date(end.getTime());  
  
        if (this.start.compareTo(this.end) > 0)  
            throw new IllegalArgumentException(start + "가 " + end + "보다 늦다.");  
    }
    
    // ...
}
```

```
수정 전: Sat Mar 30 12:10:00 KST 2024
수정 후: Sat Mar 30 12:10:00 KST 2024
```

이전과 다르게 매개변수의 유효성 검사를 복사본을 만든 이후에 한다. 이는 멀티스레딩 환경을 고려해 원본 객체 수정으로 인해 발생할 수 있는 오류를 방어하기 위함이다. 컴퓨터 보안 커뮤니티에서는 이를 검사시점/사용시점(time-of-check/time-of-use) 공격 또는 TOCTOU 공격이라 한다.

#### 확장 가능한 타입은 방어적 복사시 `clone` 메서드 활용 금지

`Date`는 `final`이 아니기 때문에 `clone`이 `Date`를 정의한게 아닐 수 있다. 그러므로 `clone`이 `Date`가 아닌 그 하위 클래스를 반환할 수 있다. 제3자에 의해 확장될 수 있는 타입은 방어적 복사본을 만들때 `clone`을 사용하면 안된다.

### 객체를 반환할 때 복사본을 활용하자

##### 수정한 접근자 - 필드의 방어적 복사본을 반환한다.

```java
public final class Period {
	
	// ...
	
	public Date start() {  
	    return new Date(start.getTime());  
	}  
	  
	public Date end() {  
	    return new Date(end.getTime());  
	}
	
	// ...
}
```

```java
public class PeriodTest {  
  
    @Test  
    void returnTest() {  
        // given  
        Date start = new Date();  
        Date end = new Date();  
        Period p = new Period(start, end);  
  
        System.out.println("수정 전: " + p.end());  
  
        // when  
        p.end().setYear(78);  
  
        // then  
        System.out.println("수정 후: " + p.end());  
    }  
}
```

```
수정 전: Sat Mar 30 15:30:12 KST 2024
수정 후: Sat Mar 30 15:30:12 KST 2024
```

## 방어적 복사본의 단점

방어적 복사는 성능 저하를 일으키고 항상 쓸 수 있는 것도 아니다. 만약 호출자가 컴포넌트 내부를 수정하지 않는다는 것을 확신한다면 복사를 생략해도 된다. 대신 해당 매개변수나 반환값을 수정하면 안된다는 것을 문서화하는게 좋다.

## 정리

클라이언트로부터 받거나 반환하는 클래스의 구성요소가 가변이라면 방어적 복사를 하는게 좋다. 하지만 방어적 복사본은 성능 저하를 일으키기도 하고 항상 쓸 수 있는게 아니다. 클라이언트가 클래스의 구성요소를 수정하지 않는다면 방어적 복사를 대신 문서화를 통해 클라이언트에게 책임이 있음을 명시하자.