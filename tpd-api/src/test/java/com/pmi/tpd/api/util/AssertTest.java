package com.pmi.tpd.api.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class AssertTest {

    @Test
    public void isTrue() {
        try {
            Assert.isTrue(false);
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        Assert.isTrue(true);
    }

    @Test
    public void isNull() {
        try {
            Assert.isNull(new Object());
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        Assert.isNull(null);
    }

    @Test
    public void notNull() {
        try {
            Assert.notNull(null);
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        Assert.notNull(new Object());
    }

    @Test
    public void hasLength() {
        try {
            Assert.hasLength(null);
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        try {
            Assert.hasLength("");
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        Assert.hasLength(" ");
        Assert.hasLength("Hello");
    }

    @Test
    public void hasText() {
        try {
            Assert.hasText(null);
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        try {
            Assert.hasText("");
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        try {
            Assert.hasText(" ");
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        Assert.hasText("12345");
        Assert.hasText(" 12345 ");
    }

    @Test
    public void doesNotContain() {
        try {
            Assert.doesNotContain("The latest fishing rods are made of fibre glass.", "rod");
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        Assert.doesNotContain("Grandma and Grandpa like sitting in their rocking chairs on the veranda.", "rod");
    }

    @Test
    public void notEmptyArray() {
        try {
            Assert.notEmpty((Object[]) null);
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        try {
            Assert.notEmpty(new String[] {});
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        Assert.notEmpty(new String[] { "1234" });
    }

    @Test
    public void noNullElements() {

        try {
            Assert.noNullElements(new String[] { null });
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        Assert.noNullElements((Object[]) null);
        Assert.noNullElements(new String[] {});
        Assert.noNullElements(new String[] { "1234" });
    }

    @Test
    public void notEmptyCollection() {
        try {
            Assert.notEmpty((Collection<?>) null);
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        try {
            Assert.notEmpty(new ArrayList<String>());
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        Assert.notEmpty(new ArrayList<>(Arrays.asList("1234")));
    }

    @Test
    public void notEmptyMap() {
        try {
            Assert.notEmpty((Map<?, ?>) null);
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        try {
            Assert.notEmpty(new HashMap<String, String>());
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        final HashMap<String, String> map = new HashMap<>();
        map.put("key", "123");
        Assert.notEmpty(map);
    }

    @Test
    public void isInstanceOf() {
        try {
            Assert.isInstanceOf(String.class, null);
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        try {
            Assert.isInstanceOf(null, null);
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        try {
            Assert.isInstanceOf(String.class, new Object());
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        Assert.isInstanceOf(String.class, "1234");
    }

    @Test
    public void isAssignable() {
        try {
            Assert.isAssignable(String.class, null);
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        try {
            Assert.isAssignable(null, null);
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        try {
            Assert.isAssignable(String.class, Object.class);
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        Assert.isAssignable(Object.class, String.class);
    }

    @Test
    public void state() {
        try {
            Assert.state(1 == 2);
            fail();
        } catch (final IllegalStateException ex) {

        }
        Assert.state(true);
    }

    @Test
    public void isEqual() {
        try {
            Assert.equalsTo("value", "1234", "0");
            fail();
        } catch (final IllegalArgumentException ex) {

        }
        try {
            Assert.equalsTo("value", null, null);
            fail();
        } catch (final NullPointerException ex) {

        }
        Assert.equalsTo("value", "1234", "1234");
    }
}
