package com.pmi.tpd.core.maintenance.event;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.api.event.advisor.event.EventType;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;

/**
 * Base specialisation for service events added to mark in-progress maintenance. These events are {@link Externalizable}
 * to allow them to be sent across the cluster. {@link Externalizable} is implemented instead of
 * {@link java.io.Serializable} because the superclass supports setting of arbitrary attributes which could be non-
 * serializable. For {@code MaintenanceEvent}, these attributes are irrelevant and therefore ignored.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class ApplicationEvent extends Event implements Externalizable {

    protected ApplicationEvent() {
        super(null, null);
    }

    protected ApplicationEvent(@Nonnull final EventType key, @Nonnull final String desc,
            @Nonnull final EventLevel level) {
        super(checkNotNull(key, "key"), checkNotNull(desc, "description"), checkNotNull(level, "level"));
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        setKey(EventAdvisorService.getInstance().getEventType(in.readUTF()).orElse(null));
        setLevel(EventAdvisorService.getInstance().getEventLevel(in.readUTF()).orElse(null));
        setDate(in.readUTF());
        setDesc(in.readUTF());
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(getKey().getType());
        out.writeUTF(getLevel().getLevel());
        out.writeUTF(getDate());
        out.writeUTF(getDesc());
    }
}
