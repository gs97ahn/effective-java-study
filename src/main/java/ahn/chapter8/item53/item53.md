# 가변인수는 신중히 사용하라

가변인수 메서드는 호출시 인수의 개수와 같은 길이의 배열이 만들어 가변인수 메서드에 건네주기 준다. 그래서 명시한 타입의 인수 0개 이상을 받을 수 있다.

##### 간단한 가변인수 활용 예

```java
public class VarargsTest {  
  
    static int sum(int... args) {  
        int sum = 0;  
        for (int arg : args)  
            sum += arg;  
        return sum;  
    }  
  
    @Test  
    void sumTest() {  
        System.out.println(sum(1, 2, 3));  
        System.out.println(sum());  
    }
}
```

```
6
0
```

##### 인수가 1개 이상이어야 하는 가변인수 메서드 - 잘못 구현한 예!

```java
public class VarargsTest {  

    static int min(int... args) {  
        if (args.length == 0)  
            throw new IllegalArgumentException("인수가 1개 이상 필요합니다.");  
        int min = args[0];  
        for (int i = 1; i < args.length; i++)  
            if (args[i] < min)  
                min = args[i];  
        return min;  
    }  
  
    @Test  
    void minTest() {  
        System.out.println(min());  
    }  
}
```

```
java.lang.IllegalArgumentException: 인수가 1개 이상 필요합니다.
```

오류는 항상 컴파일타임에 잡는게 좋지만, 위 코드에서는 인수가 0개일때 런타임에 오류를 잡는다.

##### 인수가 1개 이상이어야 할 때 가변인수를 제대로 사용하는 방법

```java
static int min(int firstArg, int... remainingArgs) {
	int min = firstArg;
	for (int arg : remiainingArgs)
		if (arg < min)
			min = arg;
	return min;
}
```

위와 같이 첫 번째로 평범한 매개변수를 받고 두 번째로 가변인수를 받으면 이전에 인수가 0개일때의 문제점이 해결 된다. 이 방식을 사용해 `printf`와 리플렉션이 재정비되었다.

## 성능이 중요할때 가변인수 활용 방법

가변인수는 성능에 치명적이다. 메서드가 호출될 때마다 배열이 새로 할당되고 초기화되기 때문이다.

이와 같은 문제 통계를 통해 해결할 수 있다. 만약 해당 메서드 호출의 95%가 인수를 3개 이하로 부른다면 아래와 같이 메서드 5개만 다중정의하자. 이를 통해 가변인수를 활용 빈도는 5%가 될수 있다.

```java
public void foo() { }  
public void foo(int a1) { }  
public void foo(int a1, int a2) { }  
public void foo(int a1, int a2, int a3) { }  
public void foo(int a1, int a2, int a3, int... rest) { }
```

## 정리

가변인수를 활용할 땐 필수 매개변수를 앞에 두자. 그리고 성능상 이슈를 고려해야 될땐 다중정의를 활용하자.