package com.pmi.tpd.api.context;

import javax.annotation.Nullable;

@FunctionalInterface
public interface IPropertyChangeCallback<T> {

  void change(@Nullable T oldValue, @Nullable T newValue);
}
