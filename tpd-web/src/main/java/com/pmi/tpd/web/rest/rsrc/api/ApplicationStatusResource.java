package com.pmi.tpd.web.rest.rsrc.api;

import javax.annotation.security.PermitAll;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

import com.codahale.metrics.annotation.Timed;
import com.google.common.annotations.VisibleForTesting;
import com.pmi.tpd.ComponentManager;
import com.pmi.tpd.api.exception.InfrastructureException;
import com.pmi.tpd.core.event.advisor.spring.lifecycle.LifecycleState;
import com.pmi.tpd.core.event.advisor.spring.lifecycle.LifecycleUtils;
import com.pmi.tpd.core.server.ApplicationState;
import com.pmi.tpd.core.server.IApplicationStatusService;
import com.pmi.tpd.startup.IStartupManager;
import com.pmi.tpd.startup.StartupUtils;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.model.Progress;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
@Path(RestApplication.API_RESOURCE_PATH + "/status")
@Tag(description = "Endpoint for application status state", name = "status")
public class ApplicationStatusResource {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationStatusResource.class);

    @GET
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Returns the current state status of application",
            responses = { @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = StateRequest.class))) })
    public Response status(@Context final HttpServletRequest req) {
        final ApplicationState state = getApplicationState(req);
        return ResponseFactory.status(mapStateToHttpStatusCode(state)).entity(StateRequest.create(state)).build();
    }

    @Path("progress")
    @GET
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Returns the progress starting status of application",
            responses = { @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = StateRequest.class))) })
    public Response status(@Context final ServletContext servletContext) {
        final IStartupManager startupManager = getStartupManager(servletContext);
        return ResponseFactory.ok(new Progress(startupManager.getProgress(), getCurrentState(servletContext))).build();
    }

    @VisibleForTesting
    protected LifecycleState getCurrentState(final ServletContext servletContext) {
        return LifecycleUtils.getCurrentState(servletContext);
    }

    @VisibleForTesting
    protected IStartupManager getStartupManager(final ServletContext servletContext) {
        return StartupUtils.getStartupManager(servletContext);
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

        // service is not available, presumably the Spring application context failed to
        // initialize.
        return ApplicationState.ERROR;
    }

    private Status mapStateToHttpStatusCode(final ApplicationState state) {
        switch (state) {
            case ERROR:
                return Status.INTERNAL_SERVER_ERROR;
            case STARTING:
            case STOPPING:
                return Status.SERVICE_UNAVAILABLE;
            default:
                return Status.OK;
        }
    }

    public static class StateRequest {

        private final ApplicationState state;

        public static StateRequest create(final ApplicationState state) {
            return new StateRequest(state);
        }

        private StateRequest(final ApplicationState state) {
            this.state = state;
        }

        public ApplicationState getState() {
            return state;
        }
    }
}
