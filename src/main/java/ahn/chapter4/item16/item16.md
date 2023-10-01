# public 클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라

##### 퇴보한 클래스는 `public`이어서는 안 된다!
```java
class Point {
    public double x;
    public double y;
}
```
- 데이터 필드에 직접 접근할 수 있으니 캡슐화의 이점을 제공하지 못한다.
- API를 수정하지 않고는
  - 내부 표현을 바꿀 수 없다.
  - 불변식을 보장할 수 없다.
  - 외부에서 필드에 접근할 때 부수 작업을 수행할 수 없다.

##### 접근자와 변경자(mutator) 메서드를 활용해 데이터를 캡슐화한다.
```java
class Point {
    private double x;
    private double y;
    
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
}
```
- 패키지 바깥에서 접근할 수 있는 클래스라면 접근자를 제공해 클래스 내부 표현 방식을 바꿀 수 있는 유연성을 얻을 수 있다.

### `package-private` 클래스 혹은 `private` 중첩 클래스라면 데이터 필드를 노출해도 하등의 문제가 없다.
- 클래스가 표현하려는 추상 개념만 올바르게 표현해주면 된다.

### `public` 클래스의 필드를 직접 노출하는 사례
성능 문제가 오늘까지 해결되지 못했기에 따라하지 말아야된다.
- `java.awt.package`
  - `Point`
  - `Dimension`

### `public` 클래스의 불변 필드
- API를 변경하지 않고는 표현 방식을 바꿀 수 없다.
- 필드를 읽을 때 부수 작업을 수행할 수 없다.
- 불변식은 보장할 수 있다.

##### 불변 필드를 노출한 `public` 클래스 - 과연 좋은가?
```java
public final class Time {
    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;
    
    public final int hour;
    public final int minute;
    
    public Time(int hour, int minute) {
        if (hour < 0 || hour >= HOURS_PER_DAY)
            throw new IllegalArgumentException("시간: " + hour);
        if (minute < 0 || minute >= MINUTES_PER_HOUR)
            throw new IllegalArgumentException("분: " + minute);
        this.hour = hour;
        this.minute = minute;
    }
    // ...
}
```