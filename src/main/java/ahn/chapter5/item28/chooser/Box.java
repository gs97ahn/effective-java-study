package ahn.chapter5.item28.chooser;

public class Box {
    private int x;
    private int y;

    public Box(int x, int y) {
        this.x = x;
        this.y =y;
    }

    void size() {
        System.out.println("x=" + x + ", y=" + y);
    }
}
