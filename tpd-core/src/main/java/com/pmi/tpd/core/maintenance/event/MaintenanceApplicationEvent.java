package com.pmi.tpd.core.maintenance.event;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.event.EventType;
import com.pmi.tpd.core.maintenance.MaintenanceType;

/**
 * Event added when a {@link MaintenanceTask} is in-progress.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class MaintenanceApplicationEvent extends ApplicationEvent {

  /** */
  private MaintenanceType type;

  /**
   *
   */
  public MaintenanceApplicationEvent() {
  }

  /**
   * @param key
   * @param desc
   * @param level
   * @param type
   */
  public MaintenanceApplicationEvent(@Nonnull final EventType key, @Nonnull final String desc,
      @Nonnull final EventLevel level, @Nonnull final MaintenanceType type) {
    super(key, desc, level);

    this.type = checkNotNull(type, "type");
  }

  /**
   * Retrieves the type of maintenance being performed by the in-progress task.
   *
   * @return the maintenance type
   */
  @Nonnull
  public MaintenanceType getType() {
    return type;
  }

  @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);

    type = (MaintenanceType) in.readObject();
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    super.writeExternal(out);

    out.writeObject(type);
  }
}
