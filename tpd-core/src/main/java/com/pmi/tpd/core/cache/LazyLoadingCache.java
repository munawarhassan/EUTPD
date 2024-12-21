/**
 * Copyright 2015 Christophe Friederich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pmi.tpd.core.cache;

import io.atlassian.util.concurrent.LazyReference;

/**
 * This class allows us to set up a cache that is lazy-loaded in a thread-safe way.
 * <p>
 * The type D is the type of the cache Data. It should normally be an immutable data object.
 * </p>
 *
 * @author devacfr
 * @since 1.0
 * @param <D>
 *            is the type of the cache Data
 */
public class LazyLoadingCache<D> {

    /** */
    private final CacheLoader<D> cacheLoader;

    /** reference must be volatile to ensure safe publication. */
    private volatile DataReference reference;

    /**
     * <p>
     * Constructor for LazyLoadingCache.
     * </p>
     *
     * @param cacheLoader
     *            a {@link com.pmi.tpd.core.cache.LazyLoadingCache.CacheLoader} object.
     */
    public LazyLoadingCache(final CacheLoader<D> cacheLoader) {
        this.cacheLoader = cacheLoader;
        reset();
    }

    /**
     * Gets the cache data object.
     * <p>
     * Calling this method may cause the cache data to be loaded, if it has not been loaded yet.
     *
     * @return the cache data object.
     */
    public D getData() {
        return reference.get();
    }

    /**
     * This method will load the latest cache data, and then replace the existing cache data.
     * <p>
     * Note that it leaves the old cache data intact while the load is occuring in order to allow readers to continue to
     * work without blocking.
     * <p>
     * This method is synchronized in order to stop a possible race condition that could publish stale data.
     *
     * @see #reset()
     */
    public synchronized void reload() {
        // Create a second DataReference while the load is happening.
        final DataReference tempReference = new DataReference();
        // force a load
        tempReference.get();
        // now swap in the loaded DataReference
        reference = tempReference;
    }

    /**
     * This method will throw away any existing cache data, and leave the LazyLoadingCache uninitialised. This means
     * that the cache will need to be loaded on the next call to getData(), and readers will be blocked until the cache
     * is reloaded.
     *
     * @see #reload()
     */
    public void reset() {
        reference = new DataReference();
    }

    /**
     * Interface to lazy-load cached data.
     *
     * @author Christophe Friederich
     * @param <D>
     *            type of data to cache.
     */
    public interface CacheLoader<D> {

        /**
         * Gets the cached data.
         *
         * @return Returns the cached data.
         */
        D loadData();
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    private class DataReference extends LazyReference<D> {

        @Override
        protected D create() throws Exception {
            return cacheLoader.loadData();
        }
    }
}
