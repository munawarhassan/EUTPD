package com.pmi.tpd.euceg.core.exporter;

import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.Streams;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.scheduler.ITaskMonitorProgress;

public interface IDataProvider<B> {

    @Nonnull
    String getAttachementFilename(@Nonnull String uuid);

    @Nonnull
    Page<B> findAll(@Nonnull final Pageable request);

    default Stream<B> stream(@Nullable final ITaskMonitorProgress monitor) {
        return Streams.stream(PageUtils.asIterable(p -> findAll(p), getInitialPageableRequest())
                .setLimit(getLimit())
                .setTaskMonitor(monitor));
    }

    @Nonnull
    Pageable getInitialPageableRequest();

    default Long getLimit() {
        return -1L;
    }
}
