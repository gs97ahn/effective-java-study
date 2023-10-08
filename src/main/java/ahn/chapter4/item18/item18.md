# 상속보다는 컴포지션을 사용하라
- 여기서 **상속**은 구현 상속만 다룬다.
  - 고려하지 말아야되는 상속:
    - 클래스가 인터페이스 구현
    - 인터페이스가 인터페이스 확장
- 상속은 코드 재사용성을 위한 강력한 수단이지만, 잘못 사용하면 오류를 내기 쉬운 소프트웨어를 만들게 된다.
  - 안전하게 상속을 하는 방법
    - 상위 클래스와 하위 클래스 모두 같은 프로그래머가 통제한다.
    - 확장할 목적으로 설계되었고 문서화도 잘 된 클래스다.
- 일반적인 구체 클래스를 패키지 경계를 넘어 상속하는 일은 위험하다.
### 메서드 호출과 달리 상속은 캡슐화를 깨뜨린다.
- 상위 클래스 구현에 따라 하위 클래스의 동작에 이상이 생길 수 있다.
  - 상위 클래스는 릴리스 마다 구현이 달라질 수 있기에 신경써야 된다.
##### `HashSet`을 사용하는 프로그램 예시
- 성능 향상을 위해
  - 원소의 수를 저장하는 변수와 접근자 메서드 추가
  - `add`와 `addAll` 재정의
```java
public class InstrumentedHashSet<E> extends HashSet<E> {
  // 추가된 원소의 수
  private int addCount = 0;

  public InstrumentedHashSet() {
  }

  public InstrumentedHashSet(int initCap, float loadFactor) {
    super(initCap, loadFactor);
  }

  @Override
  public boolean add(E e) {
    addCount++;
    return super.add(e);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
      addCount += c.size();
      return super.addAll(c);
  }
  
  public int getAddCount() {
      return addCount;
  }
}
```
위 클래스는 제대로 작동하지 않는다.
```java
@Test
void test1() {
    InstrumentedHashSet<String> s = new InstrumentedHashSet<>();
    s.addAll(List.of("틱", "탁탁", "펑"));

    System.out.println("기대값 = 3");
    System.out.println("실제값 = " + s.getAddCount());
}
```
결과
```
기대값 = 3
실제값 = 6
```
- 잘못된 부분
  - 내부 구현 방식은 HashSet 문서에 쓰여 있지 않다.
  - `addAll` 메서드 호출 전 `addCount`를 늘려준 다음 재정의 된 `add` 메서드에서 다시 `addCount`를 늘려준다.
- 해결 방법?
  - `addAll` 메서드를 재정의 하지 않는다.
    - 하지만 `addAll` 메서드에서 `add` 메서드를 이용해 구현했다는 가정한 해법이라 한계가 있다.
  - `addAll` 메서드에서 순회 하며 `add` 메서드를 한 번만 호출한다.
    - 상위 클래스의 메서드 동작을 다시 구현하는건
      - 어렵다.
      - 시간이 많이 든다.
      - 오류가 날 가능성이 늘어난다.
      - 성능을 덜어뜨릴 수 있다.
#### 상위 클래스에서 하위 클래스의 메서드 재정의로 인해 발생할 수 있는 문제
- 하위 클래스에서는 접근할 수 없는 `private` 필드를 써야 된다면 구현 자체가 불가능하다.
- 다음 릴리스에서 상위 클래스에 새로운 메서드를 추가한다면 또 다른 문제가 발생될 수 있다.
#### 보안 때문에 컬렉션에 추가된 모든 원소가 특정 조건을 만족해야만 하는 프로그램 예시
- 컬렉션을 상속하여 원소를 추가하는 모든 메서드를 재정의해 필요한 조건을 먼저 검사하면 된다.
  - 하지만 상위 클래스에 또 다른 원소 추가 메서드가 만들어진다면 다시 오류가 발생할 수 있다.
  - 다음 릴리스에서 하위 클래스에서 재정의하지 못한 새로운 메서드를 사용한다면 '허용되지 않은' 원소를 추가할 수 있게 된다.
  - 실제로 `Hashtable`과 `Vector`를 컬렉션 프레임워크에 포함히시키면서 문제 발생
### 클래스를 확장하더라도 메서드를 재정의하는 대신 새로운 메서드를 추가하면 괜찮을까?
- 훨씬 안전한 방법이지만 위험이 없는건 아니다.
  - 상위 클래스에서 하위 클래스에 추가한 메서드와 시그니처가 같지만 반환 타입이 다른 메서드가 추가되면 클래스가 컴파일 되지 않을 것이다.
  - 만약 반환 타입이 같으면 메서드 재정의한 꼴이 되어 이전에 발생한 문제가 다시 발생한다.
    - 또한, 새로운 상위 클래스의 메서드가 요규하는 규약을 만족하지 못할 가능성이 크다.
## 해결 방법
### 컴포지션(composition; 구성)
- 기존 클래스를 확장하는 대신, 새로운 클래스를 만들고 `private` 필드로 기존 클래스의 인스턴스를 참조하게 한다.
  - 기존 클래스가 새로운 클래스의 구성요소로 쓰인다는 뜻이다.
- 새 클래스의 인스턴스 메서드들은 기존 클래스의 대응하는 메서드를 호출해 그 결과를 반환한다.
  - 전달(forwarding) 방식이라 한다.
  - 새 클래스의 메서드들을 전달 메서드(forwarding method)라 부른다.
##### 상속 대신 컴포지션을 사용한 예시
```java
public class InstrumentedSet<E> extends ForwardingSet<E> {
  private int addCount = 0;

  public InstrumentedSet(Set<E> s) {
    super(s);
  }

  @Override
  public boolean add(E e) {
    addCount++;
    return super.add(e);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    addCount += c.size();
    return super.addAll(c);
  }

  public int getAddCount() {
    return addCount;
  }
}
```
```java
public class ForwardingSet<E> implements Set<E> {
    private final Set<E> s;
    public ForwardingSet(Set<E> s) { this.s = s; }

    public void clear() { s.clear(); }
    public boolean contains(Object o) { return s.contains(o); }
    public boolean isEmpty() { return s.isEmpty(); }
    public int size() { return s.size(); }
    public Iterator<E> iterator() { return s.iterator(); }
    public boolean add(E e) { return s.add(e); }
    public boolean remove(Object o) { return s.remove(o); }
    public boolean containsAll(Collection<?> c) { return s.containsAll(c); }
    public boolean addAll(Collection<? extends E> c) { return s.addAll(c); }
    public boolean removeAll(Collection<?> c) { return s.removeAll(c); }
    public boolean retainAll(Collection<?> c) { return s.retainAll(c); }
    public Object[] toArray() { return s.toArray(); }
    public <T> T[] toArray(T[] a) { return s.toArray(a); }
    @Override public boolean equals(Object o) { return s.equals(o); }
    @Override public int hashCode() { return s.hashCode(); }
    @Override public String toString() { return s.toString(); }
}
```
- `InstrumentedSet`은 `HashSet`의 모든 기능을 정의한 `Set` 인터페이스를 활용해 설계되어 견고하고 유연하다.
- 임의의 `Set`에 계측 기능을 덧씌워 새로운 `Set`으로 만드는 것이 핵심이다.
##### `Instrumented`을 이용하면 대상 `Set` 인스턴스를 특정 조건하에서만 임시로 계측할 수 있다.
```java
Set<Instant> times = new InstrumentedSet<>(new TreeSet<>(cmp));
Set<E> s = new InstrumentedSet<>(new HashSet<>(INIT_CAPACITY));
```
### 래퍼 클래스 & 데코레이터 패턴 & 위임
```java
static void walk(Set<Dog> dogs) {
    InstrumentedSet<Dog> iDog = new InstrumentedSet<>(dogs);
    // 메서드에서는 dogs 대신 iDogs를 사용한다.
    // ...
}
```
- 래러 클래스(Wrapper class)
  - 다른 `Set` 인스턴스를 감싸고(wrap) 있다는 뜻에서 `InstrumentedSet` 같은 클래스
- 데코레이터 패턴(Decorator pattern)
  - 다른 `Set`에 계측 기능을 덧씌우는 패턴
- 위임(delegation)
  - 컴포지션과 전달의 조합
#### 래퍼 클래스
- 단점이 거의 없다
- 단, **SELF 문제** 때문에 래퍼 클래스가 콜백(callback) 프레임워크와는 어울리지 않다.
  - 콜백 프레임워크란, 자기 자신의 참조를 다른 객체에 넘겨서 다음 호출때 사용하도록 한다.
  - 내부 객체는 자신을 감싸고 있는 래퍼의 존재를 모르니 자신의 참조를 넘기고, 콜백 때는 래퍼가 아닌 내부 객체를 호출하게 된다.
- 두 가지의 염려가 있지만 별다른 영향이 없다고 밝혀 졌다.
  - 전달 메서드가 주는 성능에 주는 영향
  - 래퍼 객체가 메모리 사용량에 주는 영향
### 상속은 하위 클래스가 상위 클래스의 '진짜 하위' 타입인 상황에서만 쓰여야 한다
- 클래스 B가 클래스 A와 is-a 관계일 때만 클래스 A를 상속해야 한다.
  - is-a: A는 B이다.
    - 사람은 인간이다.
    - 고양이는 동물이다.
  - has-a: A가 (부븐으로써) B를 가지고 있다.
    - 자동차는 배터리를 가지고 있다.
    - 사람은 심장을 가지고 있다.
- 다시 말해, 클래스 B는 클래스 A가 아닐 경우 상속을 사용하면 안된다.
  - A를 private 인스턴스로 두고, A와 다른 API를 제공하면 된다.
### 상속을 쓰기전 확장하려는 클래스의 API의 결함을 확인하라
- 컴포지션을 사용하면 결함을 숨기는 새로운 API를 설계하면 되지만 상속은 '그 결함까지도' 승계한다.
