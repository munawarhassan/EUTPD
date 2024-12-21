package com.pmi.tpd.core.euceg.report;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Nonnull;

import com.google.common.io.ByteSource;

/**
 * A simple {@link ITrackingReport} implementation wrapping a {@code File}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class FileTrackingReport extends ByteSource implements ITrackingReport {

    private static MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

    /** */
    private final File file;

    private String id;

    private String username;

    private String type;

    private String contentType;

    public FileTrackingReport(@Nonnull final File file, @Nonnull final String type, @Nonnull final String id,
            @Nonnull final String username) {
        this.file = checkNotNull(file, "file");
        this.type = checkNotNull(type, "type");
        this.id = checkNotNull(id, "id");
        this.username = checkNotNull(username, "username");
        this.contentType = fileTypeMap.getContentType(file);
    }

    @Override
    @Nonnull
    public String getId() {
        return id;
    }

    @Override
    @Nonnull
    public String getUsername() {
        return username;
    }

    @Override
    @Nonnull
    public String getType() {
        return type;
    }

    @Override
    public long getModified() {
        return file.lastModified();
    }

    @Nonnull
    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public @Nonnull InputStream openStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public ByteSource asByteSource() {
        return this;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

}
