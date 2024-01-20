package ahn.chapter6.item34.operation;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public enum Operation {
    PLUS("+") {
        public double apply(double x, double y) { return x + y; }
    },
    MINUS("-") {
        public double apply(double x, double y) { return x - y; }
    },
    TIMES("*") {
        public double apply(double x, double y) { return x * y; }
    },
    DIVIDE("/") {
        public double apply(double x, double y) { return x / y; }
    };

    private final String symbol;
    private static final Map<String, Operation> stringToEnum = Stream.of(values())
            .collect(toMap(Objects::toString, e -> e));

    Operation(String symbol) { this.symbol = symbol; }

    @Override public String toString() { return symbol; }

    public static Optional<Operation> fromString(String symbol) {
        return Optional.ofNullable(stringToEnum.get(symbol));
    }
    public abstract double apply(double x, double y);
}
