package com.pmi.tpd.core.mail;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.activation.DataSource;
import javax.annotation.Nonnull;

public class MailAttachment {

  /** */
  private final String fileName;

  /** */
  private final DataSource source;

  public MailAttachment(@Nonnull final String fileName, @Nonnull final DataSource source) {
    this.fileName = checkNotNull(fileName);
    this.source = checkNotNull(source);
  }

  @Nonnull
  public String getFileName() {
    return fileName;
  }

  @Nonnull
  public DataSource getSource() {
    return source;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final MailAttachment that = (MailAttachment) o;
    // Cannot check for deep equality on the DataSource, must check for reference
    // equality
    return fileName.equals(that.fileName) && source == that.source;
  }

  @Override
  public int hashCode() {
    int result = fileName.hashCode();
    result = 31 * result + source.hashCode();
    return result;
  }
}
