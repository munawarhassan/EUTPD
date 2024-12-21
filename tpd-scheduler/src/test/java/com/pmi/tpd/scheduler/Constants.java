package com.pmi.tpd.scheduler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;

/**
 * Several constants used across multiple unit tests
 */
@SuppressWarnings("ConstantConditions")
public class Constants {

    public static final JobId JOB_ID = JobId.of("job.id");

    public static final JobRunnerKey KEY = JobRunnerKey.of("test.key");

    public static final Map<String, Serializable> PARAMETERS = ImmutableMap.<String, Serializable> builder()
            .put("Hello", 42L)
            .put("World", true)
            .build();

    public static final Map<String, Serializable> EMPTY_MAP = ImmutableMap.of();

    public static final byte[] BYTES_PARAMETERS = serialize(PARAMETERS);

    public static final byte[] BYTES_EMPTY_MAP = serialize(EMPTY_MAP);

    public static final byte[] BYTES_NULL = serialize(null);

    public static final byte[] BYTES_DEADF00D = { (byte) 0xDE, (byte) 0xAD, (byte) 0xF0, (byte) 0x0D, };

    private static byte[] serialize(final Map<String, Serializable> map) {
        try {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(bytes);
            try {
                oos.writeObject(map);
                oos.flush();
                return bytes.toByteArray();
            } finally {
                oos.close();
            }
        } catch (final IOException ioe) {
            final AssertionError err = new AssertionError("Could not serialize " + map);
            err.initCause(ioe);
            throw err;
        }
    }
}
