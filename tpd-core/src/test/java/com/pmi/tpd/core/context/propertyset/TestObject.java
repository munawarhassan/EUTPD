package com.pmi.tpd.core.context.propertyset;

import java.io.Serializable;

public class TestObject implements Serializable {

    private static final long serialVersionUID = 261939103282846342L;

    private final long id;

    public TestObject(final long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final TestObject testObject = (TestObject) obj;

        return id == testObject.getId();
    }

    @Override
    public int hashCode() {
        return (int) (id ^ id >>> 32);
    }
}
