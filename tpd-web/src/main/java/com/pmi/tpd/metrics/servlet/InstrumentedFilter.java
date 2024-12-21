package com.pmi.tpd.metrics.servlet;

import static com.codahale.metrics.MetricRegistry.name;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.servlets.MetricsServlet;
import com.google.common.collect.Maps;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class InstrumentedFilter implements Filter {

    /** */
    public static final String METRIC_PREFIX = "name-prefix";

    /** */
    public static final String REGISTRY_ATTRIBUTE = MetricsServlet.class.getCanonicalName() + ".registry";

    /** */
    private static final String NAME_PREFIX = "responseCodes.";

    /** */
    private static final int OK = 200;

    /** */
    private static final int CREATED = 201;

    /** */
    private static final int NO_CONTENT = 204;

    /** */
    private static final int BAD_REQUEST = 400;

    /** */
    private static final int NOT_FOUND = 404;

    /** */
    private static final int SERVER_ERROR = 500;

    /** */
    private final String otherMetricName;

    /** */
    private final Map<Integer, String> meterNamesByStatusCode;

    /** */
    private transient MetricRegistry registry;

    /** initialized after call of init method. */
    private ConcurrentMap<Integer, Meter> metersByStatusCode;

    /** */
    private Meter otherMeter;

    /** */
    private Counter activeRequests;

    /** */
    private Timer requestTimer;

    /**
     * Creates a new instance of the filter.
     *
     * @param registry
     *            the metrics registry
     */
    public InstrumentedFilter(final MetricRegistry registry) {
        this.registry = registry;
        this.otherMetricName = NAME_PREFIX + "other";
        this.meterNamesByStatusCode = createMeterNamesByStatusCode();
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        String metricName = filterConfig.getInitParameter(METRIC_PREFIX);
        if (metricName == null || metricName.isEmpty()) {
            metricName = getClass().getName();
        }
        this.metersByStatusCode = new ConcurrentHashMap<>(meterNamesByStatusCode.size());
        for (final Entry<Integer, String> entry : meterNamesByStatusCode.entrySet()) {
            metersByStatusCode.put(entry.getKey(), registry.meter(name(metricName, entry.getValue())));
        }
        this.otherMeter = registry.meter(name(metricName, otherMetricName));
        this.activeRequests = registry.counter(name(metricName, "activeRequests"));
        this.requestTimer = registry.timer(name(metricName, "requests"));

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final StatusExposingServletResponse wrappedResponse = new StatusExposingServletResponse(
                (HttpServletResponse) response);
        activeRequests.inc();
        final Timer.Context context = requestTimer.time();
        try {
            chain.doFilter(request, wrappedResponse);
        } finally {
            context.stop();
            activeRequests.dec();
            markMeterForStatusCode(wrappedResponse.getStatus());
        }
    }

    private void markMeterForStatusCode(final int status) {
        final Meter metric = metersByStatusCode.get(status);
        if (metric != null) {
            metric.mark();
        } else {
            otherMeter.mark();
        }
    }

    private static Map<Integer, String> createMeterNamesByStatusCode() {
        final Map<Integer, String> meterNamesByStatusCode = Maps.newHashMapWithExpectedSize(6);
        meterNamesByStatusCode.put(OK, NAME_PREFIX + "ok");
        meterNamesByStatusCode.put(CREATED, NAME_PREFIX + "created");
        meterNamesByStatusCode.put(NO_CONTENT, NAME_PREFIX + "noContent");
        meterNamesByStatusCode.put(BAD_REQUEST, NAME_PREFIX + "badRequest");
        meterNamesByStatusCode.put(NOT_FOUND, NAME_PREFIX + "notFound");
        meterNamesByStatusCode.put(SERVER_ERROR, NAME_PREFIX + "serverError");
        return meterNamesByStatusCode;
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    private static class StatusExposingServletResponse extends HttpServletResponseWrapper {

        /** The Servlet spec says: calling setStatus is optional, if no status is set, the default is 200. */
        private int httpStatus = 200;

        /**
         * @param response
         */
        public StatusExposingServletResponse(final HttpServletResponse response) {
            super(response);
        }

        @Override
        public void sendError(final int sc) throws IOException {
            httpStatus = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(final int sc, final String msg) throws IOException {
            httpStatus = sc;
            super.sendError(sc, msg);
        }

        @Override
        public void setStatus(final int sc) {
            httpStatus = sc;
            super.setStatus(sc);
        }

        @Override
        public int getStatus() {
            return httpStatus;
        }
    }

}
