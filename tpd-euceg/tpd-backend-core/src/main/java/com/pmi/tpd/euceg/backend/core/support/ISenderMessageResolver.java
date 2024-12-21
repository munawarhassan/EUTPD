package com.pmi.tpd.euceg.backend.core.support;

import java.util.function.Function;

@FunctionalInterface
public interface ISenderMessageResolver<T, R> extends Function<T, R> {

}
