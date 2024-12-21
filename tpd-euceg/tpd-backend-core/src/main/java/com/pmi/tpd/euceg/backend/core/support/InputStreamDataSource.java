package com.pmi.tpd.euceg.backend.core.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;

/**
 * The InputStreamDataSource class implements a simple DataSource object that encapsulates a inputstream.
 *
 * @see javax.activation.DataSource
 * @author Christophe Friederich
 * @since 2.0
 */
public class InputStreamDataSource implements DataSource {

    /** */
    private final InputStream inputStream;

    /** */
    private final String mimeType;

    /**
     * Creates a InputStreamDataSource from a InputStream object.
     * <p>
     * <i>Note: The inputstream will not be closed until {@link DataHandler} use it.</i>
     * </p>
     *
     * @param inputStream
     *            inputstream to use.
     */
    public InputStreamDataSource(final InputStream inputStream, final String mimeType) {
        this.inputStream = inputStream;
        this.mimeType = mimeType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentType() {
        return mimeType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "InputStreamDataSource";
    }
}