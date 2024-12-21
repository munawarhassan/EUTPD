package com.pmi.tpd.core.euceg;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.api.entity.IAttachmentEntity;
import com.pmi.tpd.euceg.core.filestorage.IFileStorageFile;

/**
 * @author christophe friederich
 * @since 3.0
 */
public class DownloadableAttachment {

  /** */
  private final IFileStorageFile fileStorageFile;

  /** */
  private final IAttachmentEntity entity;

  public DownloadableAttachment(@Nonnull final IFileStorageFile fileStorageFile,
      @Nonnull final IAttachmentEntity entity) {
    this.fileStorageFile = checkNotNull(fileStorageFile, "fileStorageFile");
    this.entity = checkNotNull(entity, "entity");
  }

  @Nonnull
  public InputStream openStream() throws IOException {
    return fileStorageFile.openStream();
  }

  @Nonnull
  public String getFilename() {
    return this.entity.getFilename();
  }

  @Nonnull
  public Optional<String> getContentType() {
    return Optional.ofNullable(this.entity.getContentType());
  }
}
