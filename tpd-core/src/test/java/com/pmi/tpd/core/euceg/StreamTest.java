package com.pmi.tpd.core.euceg;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class StreamTest {

    @Test
    public void test() {
        final var stream = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)
                .parallel()
                .collect(ArrayList::new, (l, e) -> {
                    l.add(e);
                }, (l, e) -> {
                    l.addAll(e);
                });
        System.out.println(stream);
    }
}
