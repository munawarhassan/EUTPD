package com.pmi.tpd.cluster;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

/**
 * Utility methods for working with Hazelcast ObjectDataInput and ObjectDataOutput.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class HazelcastDataUtils {

    private HazelcastDataUtils() {
        // never instantiate utils classes
    }

    /**
     * @param in
     * @return
     * @throws IOException
     */
    public static List<String> readList(final ObjectDataInput in) throws IOException {
        final int length = in.readInt();
        final List<String> list = new ArrayList<>(length);
        for (int i = 0; i < length; ++i) {
            list.add(in.readUTF());
        }
        return list;
    }

    /**
     * @see #writeMap(com.hazelcast.nio.ObjectDataOutput, java.util.Map)
     */
    public static Map<String, Serializable> readMap(final ObjectDataInput in) throws IOException {
        final int length = in.readInt();
        final Map<String, Serializable> map = new HashMap<>(length);
        for (int i = 0; i < length; i++) {
            map.put(in.readUTF(), in.<Serializable> readObject());
        }
        return map;
    }

    /**
     * @param out
     * @param list
     * @throws IOException
     */
    public static void writeList(final ObjectDataOutput out, final List<String> list) throws IOException {
        out.writeInt(list.size());
        for (final String message : list) {
            out.writeUTF(message);
        }
    }

    /**
     * @see #readMap(ObjectDataInput)
     */
    public static void writeMap(final ObjectDataOutput out, final Map<String, Serializable> map) throws IOException {
        out.writeInt(map.size());
        for (final Map.Entry<String, Serializable> entry : map.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeObject(entry.getValue());
        }
    }
}
