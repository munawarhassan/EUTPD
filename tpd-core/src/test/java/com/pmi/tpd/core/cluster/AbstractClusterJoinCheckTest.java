package com.pmi.tpd.core.cluster;

import static com.google.common.io.Closeables.close;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder;
import com.pmi.tpd.cluster.ClusterJoinCheckResult;
import com.pmi.tpd.cluster.ClusterJoinMode;
import com.pmi.tpd.cluster.IClusterJoinCheck;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * Base class for unit tests for ClusterJoinCheck implementation
 */
public abstract class AbstractClusterJoinCheckTest extends MockitoTestCase {

    protected HazelcastInstance acceptHazelcast;

    protected PipedClusterJoinRequest acceptRequest;

    protected HazelcastInstance connectHazelcast;

    protected PipedClusterJoinRequest connectRequest;

    @BeforeEach
    public void setupRequest() throws IOException {
        connectHazelcast = mock(HazelcastInstance.class);
        connectRequest = new PipedClusterJoinRequest(connectHazelcast, ClusterJoinMode.CONNECT);
        acceptHazelcast = mock(HazelcastInstance.class);
        acceptRequest = new PipedClusterJoinRequest(acceptHazelcast, new DefaultSerializationServiceBuilder().build(),
                connectRequest);
    }

    protected CheckResult executeJoinCheck(final IClusterJoinCheck connectingCheck,
        final IClusterJoinCheck acceptingCheck) throws IOException, InterruptedException, ExecutionException {

        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            final List<Future<ClusterJoinCheckResult>> futures = executorService.invokeAll(Arrays.asList(() -> {
                try {
                    return connectingCheck.connect(connectRequest);
                } finally {
                    close(connectRequest, true);
                }
            }, () -> {
                try {
                    return acceptingCheck.accept(acceptRequest);
                } finally {
                    close(acceptRequest, true);
                }
            }), 10, TimeUnit.SECONDS);

            close(connectRequest, true);
            close(acceptRequest, true);

            return new CheckResult(futures.get(1).get(), futures.get(0).get());
        } finally {
            executorService.shutdown();
        }
    }

    static class CheckResult {

        final ClusterJoinCheckResult acceptResult;

        final ClusterJoinCheckResult connectResult;

        private CheckResult(final ClusterJoinCheckResult acceptResult, final ClusterJoinCheckResult connectResult) {
            this.acceptResult = acceptResult;
            this.connectResult = connectResult;
        }
    }

}
