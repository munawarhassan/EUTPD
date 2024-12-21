package com.pmi.tpd.metrics.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestHandler;

import com.codahale.metrics.jvm.ThreadDump;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An HTTP servlets which outputs a {@code text/plain} dump of all threads in the VM. Only responds to {@code GET}
 * requests.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 1.3
 */
public class ThreadDumpRequestHandler implements HttpRequestHandler {

    /** */
    private static final String CONTENT_TYPE = "application/json";

    /** */
    private transient ThreadDump threadDump;

    /** */
    private final ObjectMapper objectMapper;

    /** */
    private boolean initialized = false;

    /**
     * @param objectMapper
     */
    public ThreadDumpRequestHandler(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * @throws ServletException
     */
    protected void init() throws ServletException {
        try {
            // Some PaaS like Google App Engine blacklist java.lang.managament
            this.threadDump = new ThreadDump(ManagementFactory.getThreadMXBean());
        } catch (final NoClassDefFoundError ncdfe) {
            this.threadDump = null; // we won't be able to provide thread dump
        }
    }

    @Override
    public void handleRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        if (!initialized) {
            init();
            initialized = true;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(CONTENT_TYPE);
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        if (threadDump == null) {
            response.getWriter().println("Sorry your runtime environment does not allow to dump threads.");
            return;
        }
        final OutputStream output = response.getOutputStream();
        try {
            final ThreadDumpDescriptor descriptor = new ThreadDumpDescriptor(
                    Arrays.asList(ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)));
            objectMapper.writeValue(output, descriptor);
        } finally {
            output.close();
        }

    }

    /**
     * @author Christophe Friederich
     */
    public static final class ThreadDumpDescriptor {

        /** */
        private final List<ThreadInfo> threads;

        private ThreadDumpDescriptor(final List<ThreadInfo> threads) {
            this.threads = threads;
        }

        /**
         * @return
         */
        public List<ThreadInfo> getThreads() {
            return this.threads;
        }

    }
}