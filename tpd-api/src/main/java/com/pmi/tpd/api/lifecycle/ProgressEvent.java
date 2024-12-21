package com.pmi.tpd.api.lifecycle;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pmi.tpd.api.exec.IProgress;

/**
 * An event for following task progress {@link Progress progress}.
 * <p>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class ProgressEvent implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  protected transient Object source;

  /** */
  private final IProgress progress;

  /**
   * @param source
   * @param progress
   */
  public ProgressEvent(@Nonnull final Object source, @Nonnull final IProgress progress) {
    this.source = checkNotNull(source, "source");
    this.progress = checkNotNull(progress, "progress");
  }

  @Nonnull
  @JsonIgnore
  public Object getSource() {
    return source;
  }

  @Nonnull
  public IProgress getProgress() {
    return progress;
  }
}
