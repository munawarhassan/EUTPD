package com.pmi.tpd.metrics.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.ServletContextAware;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.json.HealthCheckModule;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Request handler implementation of {@link HealthCheckServlet}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class HealthCheckRequestHandler implements HttpRequestHandler, ServletContextAware {

    /** */
    public static final String HEALTH_CHECK_REGISTRY = HealthCheckServlet.HEALTH_CHECK_REGISTRY;

    /** */
    public static final String HEALTH_CHECK_EXECUTOR = HealthCheckServlet.HEALTH_CHECK_EXECUTOR;

    /** */
    private static final String CONTENT_TYPE = "application/json";

    /** */
    private transient HealthCheckRegistry registry;

    /** */
    private transient ExecutorService executorService;

    /** */
    private transient ObjectMapper mapper;

    /** */
    private ServletContext servletContext;

    /** */
    private boolean initialized = false;

    public HealthCheckRequestHandler(final HealthCheckRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;

    }

    protected void init(final ServletContext servletContext) {
        final Object executorAttr = servletContext.getAttribute(HEALTH_CHECK_EXECUTOR);
        if (executorAttr instanceof ExecutorService) {
            this.executorService = (ExecutorService) executorAttr;
        }

        this.mapper = new ObjectMapper().registerModule(new HealthCheckModule());
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
            final SortedMap<String, HealthCheck.Result> results = runHealthChecks();
            response.setContentType(CONTENT_TYPE);
            response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
            if (results.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            } else {
                if (isAllHealthy(results)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }

            final OutputStream output = response.getOutputStream();
            try {
                getWriter(request).writeValue(output, results);
            } finally {
                output.close();
            }
        } else {
            throw new HttpRequestMethodNotSupportedException(request.getMethod(), new String[] { "GET" },
                    "HealthCheckRequestHandler only supports GET requests");
        }
    }

    private ObjectWriter getWriter(final HttpServletRequest request) {
        final boolean prettyPrint = Boolean.parseBoolean(request.getParameter("pretty"));
        if (prettyPrint) {
            return mapper.writerWithDefaultPrettyPrinter();
        }
        return mapper.writer();
    }

    private SortedMap<String, HealthCheck.Result> runHealthChecks() {
        if (executorService == null) {
            return registry.runHealthChecks();
        }
        return registry.runHealthChecks(executorService);
    }

    private static boolean isAllHealthy(final Map<String, HealthCheck.Result> results) {
        for (final HealthCheck.Result result : results.values()) {
            if (!result.isHealthy()) {
                return false;
            }
        }
        return true;
    }
}
