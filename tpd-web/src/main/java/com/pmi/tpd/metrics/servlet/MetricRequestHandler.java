package com.pmi.tpd.metrics.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.common.Strings;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.ServletContextAware;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.codahale.metrics.servlets.MetricsServlet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.util.IOperation;
import com.pmi.tpd.web.core.support.WebHelper;

/**
 * Spring implementation of {@link com.codahale.metrics.servlets.MetricsServlet}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class MetricRequestHandler implements HttpRequestHandler, ServletContextAware {

    /** */
    public static final String RATE_UNIT = MetricsServlet.RATE_UNIT;

    /** */
    public static final String DURATION_UNIT = MetricsServlet.DURATION_UNIT;

    /** */
    public static final String SHOW_SAMPLES = MetricsServlet.SHOW_SAMPLES;

    /** */
    public static final String METRICS_REGISTRY = MetricsServlet.METRICS_REGISTRY;

    /** */
    public static final String ALLOWED_ORIGIN = MetricsServlet.ALLOWED_ORIGIN;

    /** */
    public static final String METRIC_FILTER = MetricsServlet.METRIC_FILTER;

    /** */
    public static final String CALLBACK_PARAM = MetricsServlet.CALLBACK_PARAM;

    /** */
    private static final String CONTENT_TYPE = "application/json";

    /** */
    private String allowedOrigin;

    /** */
    private String jsonpParamName;

    /** */
    private transient MetricRegistry registry;

    /** */
    private transient ObjectMapper mapper;

    /** */
    private final DelegateMetricFilter filter = new DelegateMetricFilter();

    /** */
    private ServletContext servletContext;

    /** */
    private boolean initialized = false;

    public MetricRequestHandler(final MetricRegistry registry, final ObjectMapper objectMapper) {
        this.registry = registry;
        this.mapper = objectMapper;
    }

    @Override
    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;

    }

    @Override
    public void handleRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        if (!initialized) {
            init(servletContext);
            initialized = true;
        }
        final String method = request.getMethod();
        if ("GET".equals(method)) {
            response.setContentType(CONTENT_TYPE);
            if (allowedOrigin != null) {
                response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
            }
            response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
            response.setStatus(HttpServletResponse.SC_OK);

            final OutputStream output = response.getOutputStream();

            try {
                final String filterName = request.getParameter("name");
                MetricFilter newFilter = null;
                if (Strings.hasText(filterName)) {
                    newFilter = new DefaultMetricFilter(filterName);
                } else {
                    newFilter = MetricFilter.ALL;
                }
                this.filter.execute(request, newFilter, () -> {
                    if (jsonpParamName != null && request.getParameter(jsonpParamName) != null) {
                        getWriter(request).writeValue(output,
                            new JSONPObject(request.getParameter(jsonpParamName), registry));
                    } else {
                        getWriter(request).writeValue(output, registry);
                    }
                    return null;
                });

            } finally {
                output.close();
            }
        } else {
            throw new HttpRequestMethodNotSupportedException(request.getMethod(), new String[] { "GET" },
                    "MetricRequestHandler only supports GET requests");
        }
    }

    /**
     * @param servletContext
     */
    protected void init(final ServletContext servletContext) {

        final TimeUnit rateUnit = parseTimeUnit(servletContext.getInitParameter(RATE_UNIT), TimeUnit.SECONDS);

        final TimeUnit durationUnit = parseTimeUnit(servletContext.getInitParameter(DURATION_UNIT), TimeUnit.SECONDS);

        final boolean showSamples = Boolean.parseBoolean(servletContext.getInitParameter(SHOW_SAMPLES));

        if (this.mapper == null) {
            this.mapper = new ObjectMapper()
                    .registerModule(new MetricsModule(rateUnit, durationUnit, showSamples, filter));
        }

        this.allowedOrigin = servletContext.getInitParameter(ALLOWED_ORIGIN);
        this.jsonpParamName = servletContext.getInitParameter(CALLBACK_PARAM);
    }

    private ObjectWriter getWriter(final HttpServletRequest request) {
        final boolean prettyPrint = Boolean.parseBoolean(request.getParameter("pretty"));
        if (prettyPrint) {
            return this.mapper.writerWithDefaultPrettyPrinter();
        }
        return this.mapper.writer();
    }

    private TimeUnit parseTimeUnit(final String value, final TimeUnit defaultValue) {
        try {
            return TimeUnit.valueOf(String.valueOf(value).toUpperCase(Locale.US));
        } catch (final IllegalArgumentException e) {
        }
        return defaultValue;
    }

    private static class DelegateMetricFilter implements MetricFilter {

        @Override
        public boolean matches(final String name, final Metric metric) {
            return getDelegateFilter().map(filter -> filter.matches(name, metric)).orElse(false);
        }

        Optional<MetricFilter> getDelegateFilter() {
            return WebHelper.currentHttpRequest()
                    .map(request -> (MetricFilter) request
                            .getAttribute(DelegateMetricFilter.class.getName() + "filter"));
        }

        synchronized public void execute(final HttpServletRequest request,
            final MetricFilter newFilter,
            final IOperation<Void, IOException> func) throws IOException {
            if (newFilter != null) {
                request.setAttribute(DelegateMetricFilter.class.getName() + "filter", newFilter);
            }
            func.perform();

        }

    }

    /**
     * @author Christophe Friederich
     */
    private static class DefaultMetricFilter implements MetricFilter {

        /** */
        private final String name;

        public DefaultMetricFilter(@Nonnull final String name) {
            this.name = Assert.checkHasText(name, "name");
        }

        @Override
        public boolean matches(final String name, final Metric metric) {
            return name.startsWith(this.name);
        }

    }
}