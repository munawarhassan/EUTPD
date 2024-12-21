package com.pmi.tpd.euceg.core.exporter;

import javax.annotation.Nonnull;

public interface ICallbackExport<B> {

    void forEach(@Nonnull B element);

    void summarize();
}
