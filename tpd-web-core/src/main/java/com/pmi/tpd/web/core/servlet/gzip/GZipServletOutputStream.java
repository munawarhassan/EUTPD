package com.pmi.tpd.web.core.servlet.gzip;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
class GZipServletOutputStream extends ServletOutputStream {

    /** */
    private final OutputStream stream;

    GZipServletOutputStream(final OutputStream output) throws IOException {
        super();
        this.stream = output;
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }

    @Override
    public void flush() throws IOException {
        this.stream.flush();
    }

    @Override
    public void write(final byte[] b) throws IOException {
        this.stream.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        this.stream.write(b, off, len);
    }

    @Override
    public void write(final int b) throws IOException {
        this.stream.write(b);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(final WriteListener listener) {

    }
}
