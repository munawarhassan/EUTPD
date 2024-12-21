package com.pmi.tpd.core.security.provider.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

final class EmptyNamingEnumeration<T> implements NamingEnumeration<T> {



    EmptyNamingEnumeration() {
    }

    @Override
    public T next() {
        return null;
    }

    @Override
    public boolean hasMore() {
        return false;
    }

    @Override
    public void close() throws NamingException {
    }

    @Override
    public boolean hasMoreElements() {
        return hasMore();
    }

    @Override
    public T nextElement() {
        return next();
    }
}