package com.pmi.tpd.core.maintenance.event;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.event.EventType;

/**
 * Event used to {@link com.pmi.tpd.core.maintenance.IMaintenanceService#lock() lock} the instance for system
 * maintenance. System maintenance is intentionally ambiguous, as the specifics of what might be involved vary widely.
 * <p>
 * The current use case for this is taking a full system backup. Half of the processing happens in-process, and half is
 * performed by a separate application. The system maintenance lock is used to guard the processing across both apps.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class SystemMaintenanceEvent extends ApplicationEvent {

    /** Note: There is intentionally no accessor; you can only ask if a value is right, not ask the specific value. */
    private String token;

    /**
     *
     */
    public SystemMaintenanceEvent() {
    }

    /**
     * @param key
     * @param desc
     * @param level
     * @param token
     */
    public SystemMaintenanceEvent(@Nonnull final EventType key, @Nonnull final String desc,
            @Nonnull final EventLevel level, @Nonnull final String token) {
        super(key, desc, level);

        this.token = checkNotNull(token, "token");
    }

    /**
     * Retrieves a flag indicating whether the specified value matches the token generated when maintenance was started.
     *
     * @param value
     *            the alleged token value
     * @return {@code true} if the specified value matches the token; otherwise, {@code false}
     */
    public boolean isToken(@Nullable final String value) {
        return token.equals(value);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        token = in.readUTF();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        super.writeExternal(out);

        out.writeUTF(token);
    }
}
