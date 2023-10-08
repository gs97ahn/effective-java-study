package ahn.chapter4.item17.complex;

public final class ComplexPP {
    private double re; // 실수부
    private double im; // 허수부

    public ComplexPP(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public double realPart() { return re; }
    public double imaginaryPart() { return im; }

    /**
     * 더하기
     */
    public ComplexPP add(ComplexPP c) {
        re += c.re;
        im += c.im;
        return this;
    }

    /**
     * 빼기
     */
    public ComplexPP subtract(ComplexPP c) {
        re -= c.re;
        im -= c.im;
        return this;
    }

    /**
     * 곱하기
     */
    public ComplexPP times(ComplexPP c) {
        double tmpRe = re * c.re - im * c.im;
        double tmpIm = re * c.im + im * c.re;
        re = tmpRe;
        im = tmpIm;
        return this;
    }

    /**
     * 나누기
     */
    public ComplexPP dividedByPP(ComplexPP c) {
        double tmp = c.re * c.re + c.im * c.im;
        double tmpRe = (re * c.re + im * c.im) / tmp;
        double tmpIm = (im * c.re - re * c.im) / tmp;
        re = tmpRe;
        im = tmpIm;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ComplexFP)) return false;
        ComplexPP c = (ComplexPP) o;

        return Double.compare(c.re, re) == 0
                && Double.compare(c.im, im) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(re) + Double.hashCode(im);
    }

    public String print() {
        return "(" + re + " + " + im + "i)";
    }
}
