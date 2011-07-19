package org.aklein.swap.util;

public interface Filter<T> {
    public boolean isResponsible(T obj);
}
