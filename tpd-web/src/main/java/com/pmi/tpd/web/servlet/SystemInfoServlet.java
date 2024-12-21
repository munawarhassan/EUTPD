package com.pmi.tpd.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.ComponentManager;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.exception.InfrastructureException;
import com.pmi.tpd.core.server.ApplicationState;
import com.pmi.tpd.core.server.IApplicationStatusService;
import com.pmi.tpd.startup.IStartupManager;
import com.pmi.tpd.startup.StartupUtils;
import com.pmi.tpd.web.servlet.SystemInfoServlet.SystemInfo.SystemInfoBuilder;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class SystemInfoServlet extends HttpServlet {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemInfoServlet.class);

    /** */
    public static final String PATH = "/system/info";

    /**
     *
     */
    private static final long serialVersionUID = 870234915600606406L;

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        JSON.writeValue(response.getOutputStream(),
            SystemInfo.builder().displayName(Product.getFullName()).status(getApplicationState(request)).build());
    }

    private ApplicationState getApplicationState(final HttpServletRequest request) {
        IApplicationStatusService applicationStatusService = null;

        try {
            applicationStatusService = ComponentManager.getComponentInstance(IApplicationStatusService.class);
        } catch (final BeansException | InfrastructureException e) {
            LOGGER.debug("Could not obtain ApplicationStatusService from Spring context ({})", e.getMessage());
        }

        if (applicationStatusService != null) {
            return applicationStatusService.getState();
        }

        final IStartupManager startupManager = StartupUtils.getStartupManager(request.getServletContext());
        if (startupManager.isStarting()) {
            return ApplicationState.STARTING;
        }

        // service is not available, presumably the Spring application context failed to initialize.
        return ApplicationState.ERROR;
    }

    @lombok.Getter
    @lombok.Builder
    @JsonDeserialize(builder = SystemInfoBuilder.class)
    @JsonSerialize
    public static class SystemInfo {

        /** */
        private final String displayName;

        private final ApplicationState status;

        @JsonPOJOBuilder(withPrefix = "")
        public static class SystemInfoBuilder {

        }
    }
}
