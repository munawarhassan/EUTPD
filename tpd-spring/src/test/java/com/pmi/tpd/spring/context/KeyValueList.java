package com.pmi.tpd.spring.context;

import java.util.List;

public class KeyValueList {

    private List<KeyValue> list;

    public KeyValueList() {
    }

    public KeyValueList(final List<KeyValue> l) {
        this.list = l;
    }

    public void setList(final List<KeyValue> list) {
        this.list = list;
    }

    public List<KeyValue> getList() {
        return list;
    }
}
