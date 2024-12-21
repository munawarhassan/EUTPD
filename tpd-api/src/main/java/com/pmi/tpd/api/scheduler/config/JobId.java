package com.pmi.tpd.api.scheduler.config;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A wrapper to distinguish job IDs from simple strings and to make it easier to avoid confusing them with job runner
 * keys.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@Immutable
public final class JobId implements Serializable, Comparable<JobId> {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Wraps the provided string as a {@code JobId}.
     *
     * @param id
     *           the job ID, as a string
     * @return the wrapped job runner key
     */
    @Nonnull
    public static JobId of(@Nonnull final String id) {
        return new JobId(id);
    }

    /** */
    private final String id;

    private JobId(@Nonnull final String id) {
        this.id = checkNotNull(id, "id");
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        return o != null && o.getClass() == getClass() && ((JobId) o).id.equals(id);
    }

    @Override
    public int compareTo(final JobId o) {
        return id.compareTo(o.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
