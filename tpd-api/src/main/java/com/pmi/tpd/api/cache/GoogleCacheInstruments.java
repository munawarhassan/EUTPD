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
package com.pmi.tpd.api.cache;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.collect.Maps;

/**
 * Registers cache metrics in {@link java.lang.instrument.Instrumentation}. This
 * is usually called from within a
 * component's "start" callback (e.g. {@code Startable.start()} or Spring's
 * {@code InitializingBean.afterPropertiesSet()}). Example:
 * <p/>
 *
 * <pre>
 * new GoogleCacheInstruments(&quot;cacheName&quot;).addCache(this.cacheOfThings).install();
 * </pre>
 *
 * Remember to call {@code GoogleCacheInstruments.uninstall()} in the
 * destruction callbacks, or you will cause a memory
 * leak.
 * <p/>
 * This class is <em>thread-safe</em>.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@ThreadSafe
public class GoogleCacheInstruments {

  /** logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCacheInstruments.class);

  /**
   * The name of the cache as will be shown in instrumentation.
   */
  private final String name;

  /**
   * The caches that are being monitored. If this contains more than one cache,
   * the counters are all summed up before
   * being reported to instrumentation.
   */
  private final ConcurrentMap<Cache<?, ?>, Cache<?, ?>> caches = Maps.newConcurrentMap();

  /**
   * The metrics that will be exposed in instrumentation.
   */
  // private final ImmutableList<Instrument> instruments;

  /**
   * Creates a new GoogleCacheInstruments with the given name.
   *
   * @param name
   *             the name under which the counters will appear in instrumentation
   */
  public GoogleCacheInstruments(final String name) {
    this.name = name;
    // instruments = ImmutableList.of(
    // new ExternalGauge(String.format("cache.%s.size", name), new Size()),
    // new ExternalCounter(String.format("cache.%s.hitCount", name), new
    // HitCount()),
    // new ExternalCounter(String.format("cache.%s.missCount", name), new
    // MissCount()),
    // new ExternalCounter(String.format("cache.%s.loadSuccessCount", name), new
    // LoadSuccessCount()),
    // new ExternalCounter(String.format("cache.%s.loadExceptionCount", name), new
    // LoadExceptionCount()),
    // new ExternalCounter(String.format("cache.%s.totalLoadTime", name), new
    // TotalLoadTime()),
    // new ExternalCounter(String.format("cache.%s.evictionCount", name), new
    // EvictionCount())
    // );
  }

  /**
   * <p>
   * Getter for the field <code>name</code>.
   * </p>
   *
   * @return this GoogleCacheInstruments's name
   */
  public String getName() {
    return name;
  }

  /**
   * Adds <code>cache</code> to the list of caches to expose in instrumentation.
   *
   * @param cache
   *              a Cache
   * @return this
   */
  public GoogleCacheInstruments addCache(final Cache<?, ?> cache) {
    caches.put(cache, cache);
    return this;
  }

  /**
   * Removes <code>cache</code> from the list of caches to expose in
   * instrumentation.
   *
   * @param cache
   *              a Cache
   * @return this
   */
  public GoogleCacheInstruments removeCache(final Cache<?, ?> cache) {
    caches.remove(cache);
    return this;
  }

  /**
   * Installs one instrument for each of the following statistics for all caches
   * that are monitored by this
   * GoogleCacheInstruments instance.
   * <ol>
   * <li>size</li>
   * <li>hitCount</li>
   * <li>missCount</li>
   * <li>loadSuccessCount</li>
   * <li>loadExceptionCount</li>
   * <li>totalLoadTime</li>
   * <li>evictionCount</li>
   * </ol>
   *
   * @return this
   */
  public GoogleCacheInstruments install() {
    // for (Instrument instrument : instruments)
    // {
    // Instrumentation.putInstrument(instrument);
    // }

    LOGGER.debug("Installed google cache instrumentation for: {}", getName());
    return this;
  }

  /**
   * Currently does nothing because application does not support uninstalling
   * instruments.
   *
   * @return this
   */
  public GoogleCacheInstruments uninstall() {
    LOGGER.debug("NOOP: Uninstall google cache instrumentation for: {}", getName());
    return this;
  }

  // private abstract class SumOfCacheStats implements ExternalValue
  // {
  // @Override
  // public long getValue()
  // {
  // long value = 0;
  // for (Cache cache : caches.keySet())
  // {
  // value += get(cache);
  // }
  //
  // return value;
  // }
  //
  // protected abstract long get(Cache cache);
  // }
  //
  // private class Size extends SumOfCacheStats
  // {
  // @Override
  // protected long get(Cache cache)
  // {
  // return cache.size();
  // }
  // }
  //
  // private class HitCount extends SumOfCacheStats
  // {
  // @Override
  // protected long get(Cache cache)
  // {
  // return cache.stats().hitCount();
  // }
  // }
  //
  // private class MissCount extends SumOfCacheStats
  // {
  // @Override
  // protected long get(Cache cache)
  // {
  // return cache.stats().missCount();
  // }
  // }
  //
  // private class LoadSuccessCount extends SumOfCacheStats
  // {
  // @Override
  // protected long get(Cache cache)
  // {
  // return cache.stats().loadSuccessCount();
  // }
  // }
  //
  // private class LoadExceptionCount extends SumOfCacheStats
  // {
  // @Override
  // protected long get(Cache cache)
  // {
  // return cache.stats().loadExceptionCount();
  // }
  // }
  //
  // private class TotalLoadTime extends SumOfCacheStats
  // {
  // @Override
  // protected long get(Cache cache)
  // {
  // return MILLISECONDS.convert(cache.stats().totalLoadTime(), NANOSECONDS);
  // }
  // }
  //
  // private class EvictionCount extends SumOfCacheStats
  // {
  // @Override
  // protected long get(Cache cache)
  // {
  // return cache.stats().evictionCount();
  // }
  // }
}
