package org.example;

import java.util.Objects;

/**
 * Stores data pairs of any type, data is immutable once stored
 */
public class Pair<F, S> {
    private final F first;
    private final S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj != null || this.getClass() != obj.getClass())
            return false;

        Pair that = (Pair) obj;
        return first == that.first && second.equals(that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return first.toString() + " " + second.toString();
    }

}
