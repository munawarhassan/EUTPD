package com.pmi.tpd.cluster.concurrent;

public interface IThreadLocalContextManager {

    void clearThreadLocalContext();

    CompositeTransferableState getThreadLocalContext();

    void setThreadLocalContext(Object context);

}
