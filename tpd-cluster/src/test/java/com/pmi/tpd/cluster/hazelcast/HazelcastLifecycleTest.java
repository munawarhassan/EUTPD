package com.pmi.tpd.cluster.hazelcast;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.partition.PartitionService;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class HazelcastLifecycleTest extends MockitoTestCase {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private HazelcastInstance instance;

    private HazelcastLifecycle lifecycle;

    @BeforeEach
    public void setUp() throws Exception {
        lifecycle = new HazelcastLifecycle(instance, 30);
    }

    @Test
    public void shouldDrainPartitions() {
        lifecycle.stop();

        final PartitionService partitionService = instance.getPartitionService();
        verify(partitionService).forceLocalMemberToBeSafe(30, TimeUnit.SECONDS);
    }
}
