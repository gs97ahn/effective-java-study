# equals는 일반 규약을 지켜 재정의하라.

- Class의 instance에 `equals()`를 재정의하지 않으면 오직 자기 자신과와만 같다.
- `equals()`를 재정의하는것은 쉬워보지만 생각보다 어렵다.
- `equals()`의 재정의에서 발생하는 문제를 회피하는 방법 중 하나는 재정의를 안하는것이다.

## `equals()`의 재정의가 필요없는 경우
- 각 인스턴스가 본질적으로 고유하다.
  - e.g. `Thread`
- 인스턴스의 '논리적 동치성(logical equality)'을 검사할 일이 없다.
  - e.g. `java.util.regex.Pattern`
- 상위 클래스에서 재정의한 equals가 하위 클래스에도 딱 들어맞는다.
  - e.g. `Set` `AbstractSet`, `List` `AbstractList`, `Map` `AbstractMap`
- 클래스가 private이거나 package-private이고 equals 메서드를 호출할 일이 없다.

### 실수로 호출되는 equals 방지하는 방법
```java
@Override
public boolean equals(Object o) {
    throws new AssertionError();
}
```

## `equals()`는 다음을 만족해야 된다.
- **반사성(reflexivity)**: `null`이 아닌 모든 참조 값 x에 대해, `x.equals(x)`는 `true`다.
- **대칭성(symmetry)**: `null`이 아닌 모든 참조 값 `x`, `y`에 대해, `x.equals(y)`가 `true`면 `y.equals(x)`도 `true`다.
- **추이성(transitivity)**: `null`이 아닌 모든 참조 값 `x`, `y`, `z`에 대해, `x.equals(y)`가 `true`이고 `y.equals(z)`도 `true`면 
`x.equals(z)`도 `true`다.
- **일관성(consistency)**: `null`이 아닌 모든 참조 값 `x`, `y`에 대해, `x.eqauls(y)`를 반복해서 호출하면 항 상 `true`를 반환하거나 항상 
`false`를 반환한다.
- **null-아님**: `null`이 아닌 모든 참조 값 `x`에 대해 `x.equals(null)`은 `false`다.

### 반사성
- `a.equals(a)`는 항상 참이여야 된다.
  - a = a

### 대칭성
- `a.equals(b)`가 참이면 `b.equals(a)`도 참이여야 된다.
  - a = b → b = a

```java
public final class CaseInsensitiveString {
    private final String s;

    public CaseInsensitiveString(String s) {
        this.s = Objects.requireNonNull(s);
    }
    
    // 대칭성 위배
    @Override
    public boolean equals(Object o) {
        if (o instanceof CaseInsensitiveString)
            return s.equalsIgnoreCase(((CaseInsensitiveString) o).s);
        if (o instanceof String) // 한 방향으로만 작동
            return s.equalsIgnoreCase((String) o);
        return false;
    }
}
```
---
```java
@Test
void test() {
    CaseInsensitiveString cis = new CaseInsensitiveString("Polish");
    String s = "polish";
    
    System.out.println(cis.equals(s));
    System.out.println(s.equals(cis));
}

```
출력
```
true
false
```
---
```java
@Test
void test() {
    CaseInsensitiveString cis = new CaseInsensitiveString("Polish");
    String s = "polish";
    
    List<CaseInsensitiveString> list = new ArrayList<>();
    list.add(cis);
    
    System.out.println(list.contains(s));
}
```
출력
```
false
```
다른 JDK 버전에서는 `true`나 런타임 예외가 일어나기도 한다.

---
- 그러므로 서로 다른 클래스를 비교하지 말아야 한다.
```java
public class CaseInsensitiveString {
    ...
    @Override
    public boolean equals(Object o){
        return o instanceof CaseInsensitiveString 
                && ((CaseInsensitiveString)o).s.equalsIgnoreCase(s);
    }
    ...
}
```
### 추이성
- `a.equals(b)`가 참이고 `b.equals(c)`가 참이면 `a.equals(c)`도 참이여야 된다.
  - a = b, b = c → a = c

```java
public class Point {
    private final int x;
    private final int y;
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point))
            return false;
        Point p = (Point)o;
        return p.x == x && p.y == y;
    }
}
```
```java
public class ColorPoint extends Point {
    private final Color color;
    
    public ColorPoint(int x, int y, Color color) {
        super(x, y);
        this.color = color;
    }
}
```
- `ColorPoint`에서 `equals()`가 정의되어 있지 않기 때문에 Color에 대한 정보는 무시되고 Point 정보만 비교한다.
```java
public class ColorPoint extends Point {
    ...
    // 대칭성 위배
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColorPoint))
            return false;
        return super.equals(o) && ((ColorPoint) o).color == color;
    }
    ...
}
```
---
```java
@Test
void test() {
    Point p = new Point(1, 2);
    ColorPoint cp = new ColorPoint(1, 2, Color.RED);
    
    System.out.println(p.equals(cp));
    System.out.println(cp.equals(p));
}
```
출력
```
true
false
```
`Point`를 `ColorPoint`에 비교하면 `Color`를 무시하고 `x`, `y`만 비교한다.

---

```java
public class ColorPoint {
    ...
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point))
            return false;
        return super.equals(o) && ((ColorPoint) o ).color == color;
    }
    ...
}
```
```java
@Test
void test() {
    Point p = new Point(1, 2);
    ColorPoint cp = new ColorPoint(1, 2, Color.RED);
    
    System.out.println(p.equals(cp));
    System.out.println(cp.equals(p));
}
```
출력
```
true
false
```
`Point`를 `ColorPoint`에 비교하면 `Color`를 무시하고 `x`, `y`만 비교한다.

---
```java
public class ColorPoint {
    ...
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point))
            return false;
        
        // o가 일반 Point면 색상을 무시하고 비교
        if (!(o instanceof ColorPoint))
            return o.equals(this);
        
        // o가 ColorPoint면 색상까지 비교
        return super.equals(o) && ((ColorPoint) o).color == color;
    }
    ...
}
```
```java
@Test
void test() {
    ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
    Point p2 = new Point(1, 2);
    ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);
    
    System.out.println(p1.equals(p2));
    System.out.println(p2.equals(p3));
    System.out.println(p1.equals(p1));
}
```
출력
```
true
true
false
```
- 대칭성은 지키지만 추이성이 깨진다.
- 무한 재귀에 빠질 위험이 있다.

---
- 구체 클래스를 확장해 새로운 값을 추가하면서 `equals()` 규약을 만족시킬 방법은 존재하지 않는다.

```java
public class Point {
    ...
    // 리스코프 치환 원칙 위배
    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass())
            return false;
        Point p = (Point) o;
        return p.x == x && p.y == y;
    }
    ...
}
```
- `Point`의 하위 클래스는 정의상 여전히 `Point`이므로 어디서든 `Point`로써 활용할 수 있어야되지만 위 구현은 그렇지 못하다.

```java
// 단위 원 안의 모든 점을 포함하도록 unitCircle을 초기화한다.
private static final Set<Point> unitCircle = Set.of(
        new Point(1, 0), new Point(0, 1),
        new Point(-1, 0), new Point(0, -1));

public static boolean onUnitCircle(Point p) {
    return unitCircle.contains(p);
        }
```
```java
public class CounterPoint extends Point {
    private static final AtmoicInteger counter = new AtomicInteger();
    
    public CounterPoint(int x, int y) {
        super(x, y);
        counter.incrementAndGet();
    }
    
    public static int numberCreated() {
        return counter.get();
    }
}

```
- `Point` 클래스 `equals()`가
  - `getClass` 기반이라면 틀리다.
    - `CounterPoint`의 인스턴스는 어떤 `Point`와 같지 않기 때문이다.
  - `instanceof` 기반이라면 맞다.
```java
public class ColorPoint {
  private final Point point;
  private final Color color;

  public ColorPoint(int x, int y, Color color) {
    point = new Point(x, y);
    this.color = Objects.requireNonNull(color);
  }
  
  public Point asPoint() {
      return point;
  }
  
  @Override
  public boolean equals(Object o) {
      if (!(o instanceof ColorPoint))
          return false;
      ColorPoint cp = (ColorPoint) o;
      return cp.point.equals(point) && cp.color.equals(color);
  }
}
```
- 위와 같이 구체 클래스를 확장해 값을 추가한 클래스가 종종 있다.
  - e.g. `java.sql.Timestamp`는 `java.util.Date`를 확장해 `nanoseconds` 필드를 추가

### 일관성
- `equals()`의 판단에 신뢰할 수 없는 자원이 끼어들게 해서는 안된다.

### null-아님
- `null`과 같아선 안된다.
- `equals()`에서 다음의 예외는 터지면 안된다.
  - `NullPointerException`
  - `CastException`
```java
// 명시적 null 검사 - 필요 없다!
@Override
public boolean equals(Object o) {
    if (o == null)
        return false;
    ...
}
```

```java
// 묵시적 null 검사 - 이쪽이 낫다.
@Override
public boolean equals(Object o) {
    if (!(o instanceof MyType))
        return false;
    MyType mt = (MyType) o;
        ...
}
```

#### 양질의 `equals()` 구현 방법의 단계
1. `==` 연산자를 사용해 입력이 자기 자신의 참조인지 확인한다.
2. `instanceof` 연산자로 입력이 올바른 타입인지 확인한다.
3. 입력을 올바른 타입으로 형변환한다.
4. 입력 객체와 자기 자신의 대응되는 '핵심' 필드들이 모두 일치하는지 하나씩 검사한다.

#### float과 double의 비교법
- float과 double은 Float.NaN, -0.0f, 특수한 부동소수 값 등을 다뤄야 되기 때문에 특별하다.
- `Float.equals()`와 `Double.equals()`를 사용해도 되지만 오토박싱을 수반할 수 있기 때문에 성능이 좋지 않다.
- `Float.compare()` 그리고 `Double.compare()`을 사용해야 된다.

#### 추가 주의사항
- `equals()`를 재정의할 땐 `hashCode()`도 반드시 재정의하자.
- 너무 복잡하게 해결하려고 하지말고 필드들의 동치성 검사만으로도 `equals()` 규약을 지킬 수 있다.
- Object 외의 타입을 매개변수로 받는 `equals()` 메서드는 선언하지 말자.

```java
// 잘못된 예 - 입력 타입은 반드시 Object여야 한다!
public boolean equals(MyClass o) {
        ...
}
```
- 위 방법은 `Object.equals()`를 재정의 한게 아니라 다중정의를 한것이다.

#### Tip
- AutoValue 프레임워크를 사용하면 메서드들을 알아서 만들어준다.
- IDE한테 코드를 맡기는것도 좋다.