package com.pmi.tpd.api.scheduler.config;

import static com.google.common.collect.Maps.newHashMap;
import static com.pmi.tpd.api.scheduler.config.JobConfig.NO_PARAMETERS;
import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_LOCALLY;
import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_ONCE_PER_CLUSTER;
import static com.pmi.tpd.api.scheduler.config.Schedule.forInterval;
import static com.pmi.tpd.api.scheduler.config.Schedule.runOnce;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.testing.junit5.TestCase;

public class JobConfigTest extends TestCase {

  private static final JobRunnerKey KEY = JobRunnerKey.of("test.job");

  @Test
  public void testDefaults() {
    final JobConfig config = JobConfig.forJobRunnerKey(KEY);
    assertEquals(KEY, config.getJobRunnerKey());
    assertEquals(RUN_ONCE_PER_CLUSTER, config.getRunMode());
    assertEquals(runOnce(null), config.getSchedule());
    assertEquals(NO_PARAMETERS, config.getParameters());
  }

  @Test
  public void testWithRunMode() {
    final JobConfig original = JobConfig.forJobRunnerKey(KEY);
    final JobConfig modified = original.withRunMode(RUN_LOCALLY);
    assertNotSame(original, modified);
    assertThat(modified, not(equalTo(original)));
    assertThat(original.getRunMode(), is(RUN_ONCE_PER_CLUSTER));
    assertThat(modified.getRunMode(), is(RUN_LOCALLY));
  }

  @Test
  public void testWithSchedule() {
    final Schedule schedule = forInterval(42L, new Date());
    final JobConfig original = JobConfig.forJobRunnerKey(KEY);
    final JobConfig modified = original.withSchedule(schedule);
    assertNotSame(original, modified);
    assertThat(modified, not(equalTo(original)));
    assertThat(original.getSchedule(), is(runOnce(null)));
    assertThat(modified.getSchedule(), is(schedule));
  }

  @Test
  public void testWithParametersNull() {
    final Map<String, Serializable> parameters = JobConfig.forJobRunnerKey(KEY).withParameters(null).getParameters();
    assertEquals(NO_PARAMETERS, parameters);
    assertImmutable(parameters);
  }

  @Test
  public void testWithParametersImmutableMap() {
    // ImmutableMap is special-cased because we can always be certain that its structure
    // cannot be modified. The only this that could change is mutable objects within the
    // map, and there is nothing that we could do about that anyway.
    final Map<String, Serializable> parameters = ImmutableMap.copyOf(mutableParameters());
    final JobConfig original = JobConfig.forJobRunnerKey(KEY);
    final JobConfig modified = original.withParameters(parameters);
    assertThat(modified, not(equalTo(original)));
    assertSame(parameters, modified.getParameters());
  }

  @Test
  public void testWithParametersUnmodifiableMap() {
    // With unmodifiable map, we still do the copy for two reasons:
    // 1) We can't use instanceof on it to determine that this is what it is
    // 2) It's wrapping another map that is probably still mutable, so we can't be sure
    // that its structure will not change
    final Map<String, Serializable> modifiableMap = mutableParameters();
    final Map<String, Serializable> unmodifiableMap = Collections.unmodifiableMap(modifiableMap);
    final JobConfig original = JobConfig.forJobRunnerKey(KEY);
    final JobConfig modified = original.withParameters(unmodifiableMap);
    assertThat(modified, not(equalTo(original)));
    assertThat(modified.getParameters(), equalTo(unmodifiableMap));
    assertNotSame(unmodifiableMap, modified.getParameters());
    assertImmutable(modified.getParameters());
    modifiableMap.put("Changed", "It!");
    assertThat(modified.getParameters(), equalTo(mutableParameters()));
    assertThat(modified.getParameters(), not(equalTo(unmodifiableMap)));
  }

  @Test
  public void testWithParametersNormalMap() {
    final Map<String, Serializable> modifiableMap = mutableParameters();
    final JobConfig original = JobConfig.forJobRunnerKey(KEY);
    final JobConfig modified = original.withParameters(modifiableMap);
    assertThat(modified, not(equalTo(original)));
    assertThat(modified.getParameters(), equalTo(modifiableMap));
    assertImmutable(modified.getParameters());
    modifiableMap.put("Changed", "It!");
    assertThat(modified.getParameters(), equalTo(mutableParameters()));
    assertThat(modified.getParameters(), not(equalTo(modifiableMap)));
  }

  private static void assertImmutable(final Map<String, Serializable> map) {
    try {
      map.put("This put should fail", "because the map should be immutable");
      fail("Supplied map was mutable: " + map);
    } catch (final UnsupportedOperationException uoe) {
      // Expected
    }
  }

  private static Map<String, Serializable> mutableParameters() {
    final Map<String, Serializable> map = newHashMap();
    map.put("Hello", 42L);
    map.put("World", true);
    return map;
  }
}
