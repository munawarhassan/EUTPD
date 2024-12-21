package com.pmi.tpd.spring.context;

public class KeyValue {

    private String key;

    private String value;

    public KeyValue() {
    }

    public KeyValue(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
