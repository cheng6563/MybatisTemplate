package com.mybatistemplate.util;

import java.util.Objects;

public class Pair<V0,V1> {
    private V0 value0;
    private V1 value1;

    @Override
    public String toString() {
        return "Pair{" +
                "value0=" + value0 +
                ", value1=" + value1 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(value0, pair.value0) &&
                Objects.equals(value1, pair.value1);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value0, value1);
    }

    public V0 getValue0() {

        return value0;
    }

    public void setValue0(V0 value0) {
        this.value0 = value0;
    }

    public V1 getValue1() {
        return value1;
    }

    public void setValue1(V1 value1) {
        this.value1 = value1;
    }

    public Pair(V0 value0, V1 value1) {

        this.value0 = value0;
        this.value1 = value1;
    }
}
