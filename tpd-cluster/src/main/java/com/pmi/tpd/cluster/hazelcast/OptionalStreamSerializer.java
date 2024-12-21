package com.pmi.tpd.cluster.hazelcast;

import java.io.IOException;
import java.util.Optional;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

/**
 * Serializes Fugue's {@link Optional}.
 * <p>
 * The primary motivator for the existence of this class is to allow the <i>data</i> inside an {@link Optional} to be
 * serialized.
 * <p>
 * Note: Future versions of Hazelcast are supposed to allow hijacking serialization even for fields inside normal
 * {@link java.io.Serializable Serializable} types, which would render this serializer unnecessary. However, they have
 * warned that <i>doing</i> so is very slow, so it may be desirable to retain this serializer anyway.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class OptionalStreamSerializer implements StreamSerializer<Optional<?>> {

    @Override
    public void destroy() {
    }

    @Override
    public int getTypeId() {
        return HazelcastConstants.TYPE_OPTION;
    }

    @Override
    public Optional<?> read(final ObjectDataInput in) throws IOException {
        final boolean defined = in.readBoolean();

        return defined ? Optional.of(in.readObject()) : Optional.empty();
    }

    @Override
    public void write(final ObjectDataOutput out, final Optional<?> option) throws IOException {
        final boolean defined = option.isPresent();
        out.writeBoolean(defined);
        if (defined) {
            out.writeObject(option.get());
        }
    }
}
