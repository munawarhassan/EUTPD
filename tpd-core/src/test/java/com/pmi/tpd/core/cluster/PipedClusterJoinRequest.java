package com.pmi.tpd.core.cluster;

import static com.google.common.base.Preconditions.checkState;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;

import com.google.common.io.Closeables;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.internal.serialization.impl.ObjectDataInputStream;
import com.hazelcast.internal.serialization.impl.ObjectDataOutputStream;
import com.hazelcast.internal.serialization.impl.SerializationUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.pmi.tpd.cluster.ClusterJoinMode;
import com.pmi.tpd.cluster.IClusterJoinRequest;

/**
 * {@link ClusterJoinRequest} implementation for unit testing join checks
 */
public class PipedClusterJoinRequest implements IClusterJoinRequest, Closeable {

    private final HazelcastInstance hazelcast;

    private final ClusterJoinMode joinMode;

    private ObjectDataInputStream input;

    private ObjectDataOutputStream output;

    public PipedClusterJoinRequest(final HazelcastInstance hazelcast, final ClusterJoinMode joinMode) {
        this.joinMode = joinMode;
        this.hazelcast = hazelcast;
    }

    public PipedClusterJoinRequest(final HazelcastInstance hazelcast,
            final InternalSerializationService serializationService, final PipedClusterJoinRequest other)
            throws IOException {
        checkState(other.input == null, "Pipe already connected");
        this.hazelcast = hazelcast;
        this.joinMode = other.joinMode == ClusterJoinMode.ACCEPT ? ClusterJoinMode.CONNECT : ClusterJoinMode.ACCEPT;

        // Connect streams one way
        connect(serializationService, this, other);
        // And now back the other way
        connect(serializationService, other, this);
    }

    private void connect(final InternalSerializationService serializationService,
        final PipedClusterJoinRequest source,
        final PipedClusterJoinRequest sink) throws IOException {
        final PipedStreams streams = new PipedStreams(32 * 1024);

        source.input = SerializationUtil.createObjectDataInputStream(streams.input(), serializationService);
        sink.output = SerializationUtil.createObjectDataOutputStream(streams.output(), serializationService);
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
        checkState(input != null, "Pipe not connected");
        return input;
    }

    @Nonnull
    @Override
    public ObjectDataOutput out() {
        checkState(output != null, "Pipe not connected");
        return output;
    }

    @Override
    public void close() throws IOException {
        Closeables.close(input, true);
        Closeables.close(output, true);
    }

    /**
     * Simple alternative to PipedInputStream / PipedOutputStream that uses an internal circular buffer. This is faster
     * than PipedInputStream / PipedOutputStream because they use wait(1000) internally without notifying, which leads
     * to a full second wait when a read blocks waiting for data to be written.
     */
    private static class PipedStreams {

        private final Object sync;

        private final byte[] buffer;

        private volatile boolean closed;

        // readIndex == writeIndex signifies an empty buffer.
        // (writeIndex + 1) % buffer.length == readIndex signifies a full buffer.
        private volatile int readIndex;

        private volatile int writeIndex;

        public PipedStreams(final int bufferSize) {
            // A full buffer has one unused byte, so allocate one more than the requested bufferSize.
            buffer = new byte[bufferSize + 1];
            sync = new Object();
        }

        public InputStream input() {
            return new InputStream() {

                @Override
                public int read() throws IOException {
                    int result = -1;
                    synchronized (sync) {
                        while (readIndex == writeIndex && !closed) {
                            try {
                                sync.wait();
                            } catch (final InterruptedException e) {
                                throw new IOException("Interrupted!");
                            }
                        }
                        if (readIndex != writeIndex) {
                            result = buffer[readIndex] & 0xff;
                            readIndex = (readIndex + 1) % buffer.length;
                            // signal that there's free space
                            sync.notify();
                        }
                    }
                    return result;
                }
            };
        }

        public OutputStream output() {
            return new OutputStream() {

                @Override
                public void write(final int b) throws IOException {
                    synchronized (sync) {
                        while ((writeIndex + 1) % buffer.length == readIndex && !closed) {
                            // reader is a full buffer behind
                            try {
                                sync.wait();
                            } catch (final InterruptedException e) {
                                throw new IOException("Interrupted!");
                            }
                        }
                        if (closed) {
                            throw new IOException("Pipe closed");
                        }

                        buffer[writeIndex] = (byte) b;
                        writeIndex = (writeIndex + 1) % buffer.length;
                        // notify the reader that there's data to be read
                        sync.notify();
                    }
                }

                @Override
                public void close() throws IOException {
                    synchronized (sync) {
                        closed = true;
                        sync.notify();
                    }
                }
            };
        }
    }
}
