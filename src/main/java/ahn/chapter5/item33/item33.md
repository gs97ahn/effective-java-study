# 타입 안전 이종 컨테이너를 고려하라

## 단일원소 컨테이너

제네릭은 `Set<E>`, `Map<K, V>` 등의 컬렉션 뿐 아니라 `ThreadLocal<T>`, `AtomicReference<T>`와 같은 단일원소 컨테이너에서도 자주 사용됩니다. 
하지만 매개변수화 되는 대상이 원소가 아닌 컨테이너 자신이기에 하나의 컨테이너에서 매개변수화할 수 있는 타입의 수에 제한있습니다. 하지만 더욱 유연한 수단이 
필요할 때가 있습니다.

## 타입 안전 이종 컨테이너 패턴(type safe heterogenous container pattern)

타입 안전 이종 컨테이너 패턴은 컨테이너 대신 키를 매개변수화 해서 컨테이너에 값을 넣거나 뺄때 매개변수화한 키를 함께 제공하는 패턴입니다. 이 패턴을 통해 
제네릭 타입 시스템이 값의 타입이 키와 같음을 보장할 수 있습니다.

타입별로 즐겨 찾는 인스턴스를 저장하고 검색할 수 있는 `Favorites` 클래스를 예시로 들 수 있습니다. 여기서 각 타입의 `Class` 객체를 매개변수화한 키로 
작동하는데, 이는 `class`의 클래스가 제네릭이기 때문에 가능합니다. 즉, `String.class`의 타입은 `Class<String>`이고 `Integer.class`에 타입은 
`Class<Integer>`입니다. 그리고 컴파일타임과 런타임에 타입 정보를 알아내기 위해 메서드들이 주고 받는 `class` 리터럴을 타입 토큰(type token)이라 
합니다.

```java
public class Favorites {  
  
    private Map<Class<?>, Object> favorites = new HashMap<>();  
  
    public <T> void putFavorite(Class<T> type, T instance) {  
        favorites.put(Objects.requireNonNull(type), instance);  
    }  
  
    public <T> T getFavorite(Class<T> type) {  
        return type.cast(favorites.get(type));  
    }  
}
```

```java
public class FavoritesTest {  
  
    @Test  
    void test1() {  
        Favorites f = new Favorites();  
  
        f.putFavorite(String.class, "Java");  
        f.putFavorite(Integer.class, 0xcafebabe);  
        f.putFavorite(Class.class, Favorites.class);  
  
        String favoriteString = f.getFavorite(String.class);  
        int favoriteInteger = f.getFavorite(Integer.class);  
        Class<?> favoriteClass = f.getFavorite(Class.class);  
  
        System.out.printf("%s %x %s%n", favoriteString, favoriteInteger, favoriteClass.getName());  
    }  
}
```

```
Java cafebabe Favorites
```

`Favorites`에 변수인 `favorites`의 타입은 비한정적 와일드카드 타입인 `Map<Class<?>, Object>`이라 아무것도 넣을 수 없어 보이지만, 와일드카드 
타입이 중첩되었기에 그 반대입니다.

그리고 `favoirtes`의 값 타입은 `Object`이라 키와 값 사이의 타입 관계를 보증할 수 없습니다.

### `putFavorite()` 메서드

`Class` 객체와 즐겨찾기 인스턴스를 `favorites`에 클라이언트가 관계를 지어주는데, 키와 값 사이의 '타입 링크(type linkage)' 정보는 버려집니다. 
그리고 `getFavorite()` 메서드를 호출하게 되면 이 관계를 되살릴 수 있습니다.

### `getFavorite()` 메서드

`favorites` 값 타입은 `Object`이기에 객체를 꺼낼때 동적으로 `Object`를 `cast()` 메서드를 통해 `T`로 변환해 주게 됩니다. `cast()` 메서드는 
`Class` 객체가 알려주는 타입의 인스턴스인지 검사를 합니다. 그리고 맞으면 그 인수를 그대로 반환하고, 아니면 `ClassCastException`을 던집니다. `Map` 
안에 값이 해당 키와 타입이 일치하는데 굳이 `cast()` 메서드를 활용합니다. 그 이유는 메서드의 시그니처가 `Class` 클래스가 제네릭이라는 이점을 완벽히 
활용하여 비검사 형변환하는 손실 없이 `Favorites`를 타입 안전하게 만들어 주기 때문입니다.

```java
public class Class<T> {
	
	@SuppressWarnings("unchecked")  
	@HotSpotIntrinsicCandidate  
	public T cast(Object obj) {  
	    if (obj != null && !isInstance(obj))  
	        throw new ClassCastException(cannotCastMsg(obj));  
	    return (T) obj;  
	}
}
```

### 제약 사항

첫 번째로 클라이언트가 `Class` 객체를 raw 타입으로 넘기면 `Favorites` 인스턴스의 타입 안전성이 깨집니다. 하지만 컴파일타임시 비검사 경고가 발생하게 
됩니다. 만약 타입 불변식을 보장하고 싶으면 아래와 같이 `instance`의 타입이 `type`으로 명시한 타입과 같은지 확인하면 됩니다. 
`java.util.Collections`에 `checkedSet()`, `checkedList()`, `checkedMap()` 같은 메서드들도 이와 같은 방법을 활용합니다.

```java
public <T> void putFavorite(Class<T> type, T instance) {
    favorites.put(Objects.requireNonNull(type), type.cast(instance));
}
```

두 번째로 실체화 불가 타입에는 사용할 수 없습니다. 즉, `String`이나 `String[]`은 저장할 수 있지만 `List<String>`에는 저장할 수 없습니다. 이는 
`List<String>`에 `Class` 객체를 얻을 수 없기 때문입니다. 아래와 같이 슈퍼 타입 토큰(super type token)을 사용한다면 `List<String>`도 저장할 
수 있지만, 완벽하지는 않은 방법입니다.

## 한정적 타입 토큰과 비한정적 타입 토큰

어떤 Class 객체든 허용하고 싶다면 비한정적 타입 토큰을 활용하면 되고, 특정 Class 객체만 허용하고 싶다면 한정적 타입 토큰을 활용하면 됩니다. 한정적 타입 
토큰은 한정적 타입 매개변수나 한정적 와일드카드를 사용하여 표현 가능한 타입을 제한하는 타입 토큰입니다.

한정적 타입 토큰은 애너테이션 API에서 적극적으로 사용됩니다. 아래의 코드는 `AnnotatedElement` 인터페이스에서 선언된 메서드로, 대상 요소에 달려 잇는 
애너테이션을 읽어 오는 기능을 합니다. 구체적으로 이 메서드는 리플렉션의 대상이 되는 타입들, 즉 클래스(`java.lang.Class<T>`), 
메서드(`java.lang.reflect.Method`), 필드(`java.lang.reflect.Field`) 같이 프로그램 요소를 표현하는 타입들에서 구현합니다.

```java
public <T extends Annotation> T getAnnotation(Class<T> annotationType);
```

애너테이션 타입에 해당하는 클래스 객체인 `annotationType` 인수는 애너테이션 타입을 뜻하는 한정적 타입 토큰이며, 이 메서드는 토큰으로 명시한 타입의 
애너테이션이 대상 요소에 달려 있다면 그 애너테이션을 반환하고, 없다면 null을 반환합니다. 그래서 애너테이션된 요소는 그 키가 애너테이션 타입인 타입 안전 
이종 컨테이너니 것입니다.

`Class<?>` 타입 객체를 한정적 타입 토큰을 받는 메소드로 넘기려면 `Class<? extends Annotation>`으로 형변환하면 됩니다. 하지만 형변환이 비검사이기에
컴파일시 경고가 발생합니다. 그래서 `Class` 객체에서 동적으로 안전하게 형변환을 해주는 `asSubclass` 메서드를 활용하는게 좋습니다. 형변환에 성공하면 
클래스 객체를 반환하고, 실패하면 `ClassCastException`을 던지게 됩니다.

##### `asSubclass()`를 사용해 한정적 타입 토큰을 안전하게 형변환한다.

```java
static Annotation getAnnotation(AnnotatedElement element, String annotationTypeName) {
	Class<?> annotationType = null; // 비한정적 타입 토큰
	try {
		annotationType = Class.forName(annotationTypeName);
	} catch (Exception ex) {
		throw new IllegalArgumentException(ex);
	}
	return element.getAnnotation(annotationType.asSubclass(Annotation.class));
}
```
## 정리

컬렉션 API 같은 제네릭 형태에서는 한 컨테이너가 다룰 수 있는 타입 매개변수의 수가 고정되어 있습니다. 컨테이너 자체가 아닌 키를 타입 매개변수로 바꾸면 타입 
안전 이종 컨테이너를 만들 수 있습니다. 이때 `Class`를 키워드로 사용하며 `Class` 객체를 타입 토큰이라 합니다.