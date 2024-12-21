package com.pmi.tpd.api.paging;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.scheduler.ITaskMonitorProgress;

/**
 * Represents a iterable page provided by a {@link IPageProvider}.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @param <T>
 *            the type contained in page.
 */
public class PagedIterable<T> implements Iterable<T> {

    /** */
    private final IPageProvider<T> pageProvider;

    /** */
    private final Pageable pageRequest;

    @Nullable
    private ITaskMonitorProgress taskMonitor;

    /** */
    private long maximumElement = -1;

    /**
     * Create new instance of {@link PagedIterable}.
     *
     * @param pageProvider
     *                     the provider of page.
     * @param pageSize
     *                     size of page.
     */
    public PagedIterable(final IPageProvider<T> pageProvider, final int pageSize) {
        this(pageProvider, PageUtils.newRequest(0, pageSize));
    }

    /**
     * Create new instance of {@link PagedIterable}.
     * 
     * @param pageProvider
     *                     the provider of page.
     * @param pageRequest
     *                     the page request.
     */
    public PagedIterable(final IPageProvider<T> pageProvider, final Pageable pageRequest) {
        this.pageProvider = pageProvider;
        this.pageRequest = pageRequest;
    }

    @Nonnull
    public PagedIterable<T> setTaskMonitor(final ITaskMonitorProgress taskMonitor) {
        this.taskMonitor = taskMonitor;
        return this;
    }

    @Nonnull
    public PagedIterable<T> setLimit(final long maximumElement) {
        this.maximumElement = maximumElement;
        return this;
    }

    @Override
    public Iterator<T> iterator() {
        final var page = pageProvider.get(pageRequest);
        if (taskMonitor != null) {
            taskMonitor.started(page.getTotalElements());
        }
        return new PagedIterator(page, taskMonitor, maximumElement);
    }

    private class PagedIterator implements Iterator<T> {

        /** */
        private Page<T> currentPage;

        /** */
        private Iterator<T> iterator;

        private final long maximumElement;

        private long counter;

        @Nullable
        private final ITaskMonitorProgress taskMonitor;

        PagedIterator(final Page<T> page, @Nullable final ITaskMonitorProgress taskMonitor, final long maxElement) {
            this.currentPage = page;
            this.iterator = page.getContent().iterator();
            this.maximumElement = maxElement;
            this.counter = 0;
            this.taskMonitor = taskMonitor;
        }

        @Override
        public boolean hasNext() {
            if (this.maximumElement > 0 && counter >= this.maximumElement) {
                return false;
            }
            final var next = iterator.hasNext() || !currentPage.isLast();
            return next;
        }

        @Override
        public T next() {
            if (!iterator.hasNext()) {
                currentPage = pageProvider.get(currentPage.nextPageable());
                iterator = currentPage.getContent().iterator();
            }
            counter++;
            if (taskMonitor != null) {
                taskMonitor.increment();
            }
            return iterator.next();
        }

        @Override
        public void remove() {
            iterator.remove();
        }

    }
}
