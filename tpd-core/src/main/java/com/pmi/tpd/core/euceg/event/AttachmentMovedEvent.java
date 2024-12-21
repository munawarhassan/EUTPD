package com.pmi.tpd.core.euceg.event;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.audit.AuditEntryBuilder;
import com.pmi.tpd.api.audit.AuditEntryConverter;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.audit.annotation.Audited;
import com.pmi.tpd.api.event.BaseEvent;

/**
 * Raised when a attachment is created.
 *
 * @since 3.0
 * @author devacfr
 */
@Audited(converter = AttachmentMovedEvent.AuditConverter.class, priority = Priority.HIGH, channels = {
    EucegChannels.EUCEG, EucegChannels.ATTACHMENT })
public class AttachmentMovedEvent extends BaseEvent {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final String attachmentId;

  /** */
  private final String filename;

  /** */
  private final String path;

  /** */
  private final String newPath;

  /** */
  private final String mimeType;

  /** */
  private final long size;

  /**
   * Default constructor.
   *
   * @param source
   *                 The object on which the Event initially occurred.
   * @param filename
   *                 the filename.
   * @param path
   *                 the path
   */
  public AttachmentMovedEvent(@Nonnull final Object source, @Nonnull final String attachmentId,
      @Nonnull final String filename, @Nullable final String mimetype, @Nonnull final String path,
      @Nonnull final String newPath, final long size) {
    super(source);
    this.attachmentId = checkNotNull(attachmentId, "attachmentId");
    this.filename = checkNotNull(filename, "filename");
    this.path = checkNotNull(path, "path");
    this.newPath = checkNotNull(newPath, "newPath");
    this.mimeType = mimetype;
    this.size = size;
  }

  @Nonnull
  public String getAttachmentId() {
    return attachmentId;
  }

  @Nonnull
  public String getFilename() {
    return filename;
  }

  @Nonnull
  public String getPath() {
    return path;
  }

  @Nonnull
  public String getNewPath() {
    return newPath;
  }

  @Nullable
  public String getMimeType() {
    return mimeType;
  }

  public long getSize() {
    return size;
  }

  public static class AuditConverter implements AuditEntryConverter<AttachmentMovedEvent> {

    @Override
    public IAuditEntry convert(final AttachmentMovedEvent event, final AuditEntryBuilder builder) {
      final ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.<String, String>builder()
          .put("attachmentId", event.attachmentId)
          .put("filename", event.filename)
          .put("path", event.path)
          .put("newPath", event.newPath)
          .put("size", Long.toString(event.size));
      if (event.mimeType != null) {
        mapBuilder.put("mimeType", event.mimeType);
      }
      return builder.details(mapBuilder.build()).build();
    }

  }

}