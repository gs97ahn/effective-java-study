package ahn.chapter4.item17.complex;

public final class ComplexFP {
    private final double re; // 실수부
    private final double im; // 허수부

    public ComplexFP(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public double realPart() { return re; }
    public double imaginaryPart() { return im; }

    /**
     * 더하기
     */
    public ComplexFP plus(ComplexFP c) {
        return new ComplexFP(re + c.re, im + c.im);
    }

    /**
     * 빼기
     */
    public ComplexFP minus(ComplexFP c) {
        return new ComplexFP(re - c.re, im - c.im);
    }

    /**
     * 곱하기
     */
    public ComplexFP times(ComplexFP c) {
        return new ComplexFP(re * c.re - im * c.im,
                re * c.im + im * c.re);
    }

    /**
     * 나누기
     */
    public ComplexFP dividedBy(ComplexFP c) {
        double tmp = c.re * c.re + c.im * c.im;
        return new ComplexFP((re * c.re + im * c.im) / tmp,
                (im * c.re - re * c.im) / tmp);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ComplexFP)) return false;
        ComplexFP c = (ComplexFP) o;

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
