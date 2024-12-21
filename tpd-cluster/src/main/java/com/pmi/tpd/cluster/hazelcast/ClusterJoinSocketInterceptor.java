package com.pmi.tpd.cluster.hazelcast;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.instance.impl.HazelcastInstanceImpl;
import com.hazelcast.internal.serialization.SerializationService;
import com.hazelcast.internal.serialization.impl.ObjectDataInputStream;
import com.hazelcast.internal.serialization.impl.ObjectDataOutputStream;
import com.hazelcast.internal.serialization.impl.SerializationUtil;
import com.hazelcast.nio.MemberSocketInterceptor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spring.context.SpringAware;
import com.pmi.tpd.cluster.ClusterJoinMode;
import com.pmi.tpd.cluster.IClusterJoinManager;
import com.pmi.tpd.cluster.IClusterJoinRequest;

/**
 * A {@link MemberSocketInterceptor} which delegates to {@link IClusterJoinManager} to verify if a member can join.
 * <p>
 * Unfortunately this cannot be a singleton as it requires access to the {@link Node node's}
 * {@link SerializationService}
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@SpringAware
public class ClusterJoinSocketInterceptor implements MemberSocketInterceptor, HazelcastInstanceAware {

    private IClusterJoinManager clusterJoinManager;

    private HazelcastInstanceImpl hazelcast;

    @Override
    public void init(final Properties properties) {
        // no-op
    }

    @Override
    public void onAccept(final Socket socket) throws IOException {
        clusterJoinManager.accept(new SocketClusterJoinRequest(socket, hazelcast, ClusterJoinMode.ACCEPT));
    }

    @Override
    public void onConnect(final Socket socket) throws IOException {
        clusterJoinManager.connect(new SocketClusterJoinRequest(socket, hazelcast, ClusterJoinMode.CONNECT));
    }

    @Autowired
    public void setClusterJoinManager(final IClusterJoinManager clusterJoinManager) {
        this.clusterJoinManager = clusterJoinManager;
    }

    @Override
    public void setHazelcastInstance(final HazelcastInstance hazelcast) {
        this.hazelcast = (HazelcastInstanceImpl) hazelcast;
    }

    /**
     * @author Christophe Friederich
     */
    static class SocketClusterJoinRequest implements IClusterJoinRequest {

        /** */
        private final ClusterJoinMode joinMode;

        /** */
        private final HazelcastInstanceImpl hazelcast;

        /** */
        private final ObjectDataInputStream in;

        /** */
        private final ObjectDataOutputStream out;

        SocketClusterJoinRequest(final Socket socket, final HazelcastInstanceImpl hazelcast,
                final ClusterJoinMode joinMode) throws IOException {
            this.joinMode = joinMode;
            this.hazelcast = hazelcast;
            in = SerializationUtil.createObjectDataInputStream(socket.getInputStream(),
                hazelcast.getSerializationService());
            out = SerializationUtil.createObjectDataOutputStream(socket.getOutputStream(),
                hazelcast.getSerializationService());
        }

        @Nonnull
        @Override
        public HazelcastInstance getHazelcast() {
            return hazelcast;
        }

        @Nonnull
        @Override
        public ClusterJoinMode getJoinMode() {
            return joinMode;
        }

        @Nonnull
        @Override
        public ObjectDataInput in() {
            return in;
        }

        @Nonnull
        @Override
        public ObjectDataOutput out() {
            return out;
        }
    }
}
