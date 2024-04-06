# null이 아닌, 빈 컬렉션이나 배열을 반환하라

##### 컬렉션이 비었으면 `null`을 반환한다. - 따라 하지 말 것!

```java
private final Listh<Cheese> cheeseInStock = ...;

/**
 * @return 매장 안에 모든 치즈 목륵을 반환한다.
 *     단, 재고가 하나도 없다면 null을 반환한다.
 */
 public List<Cheese> getCheese() {
	 return cheeseInStock.isEmpty() ? null : new ArrayList<>(cheeseInStock);
}
```


이와 같은 코드는 `NPE`를 발생시킬 수 있고 클라이언트가 아래와 같이 `null`에 대한 처리를 해야 되기 때문에 클라이언트 친화적이지 않다.

```java
List<Cheese> cheeses = shop.getCheeses();
if (cheese != null && cheeses.contains(Cheese.STILTON))
	System.out.println("좋았어, 바로 그거야.");
```

## `null`을 반환하면 안되는 이유

`List.of()`와 같이 빈 컨테이너는 비용이 발생하기 때문에 `null`을 반환하는 게 낫다는 주장이 있다. 하지만 다음과 같이 이유 때문에 잘못된 주장이라 볼 수 있다.

1. 성능 차이가 신경 쓸 수준이 못된다.
2. 배열은 새로 할당하지 않고도 반환할 수 있다.

##### 빈 컬렉션을 반환하는 올바른 예

```java
public List<Cheese> getCheese() {
	return new ArrayList<>(cheeseInStock);
}
```

빈 컬렉션을 할당하면 성능 이슈가 발생할 수 있다. 하지만 이는 `Collection.emptyList()`, `Collection.emptySet()`, `Collection.emptyMap()`와 같은 불변 컬렉션을 활용하여 쉽게 해결할 수 있다.

##### 최적화 - 빈 컬렉션을 매번 새로 할당하지 않도록 했다.

```java
public List<Cheese> getCheeses() {
	return cheesesInStock.isEmpty() ? Collections.emtpyList() : new ArrayList<>(cheeseInStock);
}
```

## 배열도 `null`이 아닌 빈 배열을 반환하자

##### 길이가 0일 수도 있는 배열을 반환하는 올바른 방법

```java
public Cheese[] getCheeses() {
	return cheesesInStock.toArray(new Cheese[0]);
}
```

배열의 길이는 불변이기 때문에 성능 이슈가 발생하지 않는다. 하지만 더욱 확실하게 빈 배열조차 새롭게 만들고 싶지 않다면 다음과 같은 방법으로 해결할 수 있다.

##### 최적화 - 빈 배열을 매번 새로 할당하지 않도록 했다.

```java
private static final Cheese[] EMPTY_CHEESE_ARRAY = new Cheese[0];

public Cheese[] getCheeses() {
	return cheesesInStock.toArray(EMPTY_CHEESE_ARRAY);
}
```

## 정리

`null`을 반환하는 API는 오류 가능성이 늘어나고 성능상 좋지도 않기 때문에 빈 배열이나 컬렉션을 활용하자.