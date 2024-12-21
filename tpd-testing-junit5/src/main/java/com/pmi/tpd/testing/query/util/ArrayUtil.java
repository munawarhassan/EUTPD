package com.pmi.tpd.testing.query.util;

import java.util.stream.Stream;

public class ArrayUtil {

    @SuppressWarnings("unchecked")
    public static <T> T[] merge(final T[] arr1, final T[] arr2) {
        return (T[]) Stream.of(arr1, arr2).flatMap(Stream::of).toArray();
    }

    public static <T> T[] add(final T[] arr1, final T one) {

        return merge(arr1, single(one));
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] single(final T one) {
        final Object[] genericArray = new Object[] { one };
        return (T[]) genericArray;
    }
}
