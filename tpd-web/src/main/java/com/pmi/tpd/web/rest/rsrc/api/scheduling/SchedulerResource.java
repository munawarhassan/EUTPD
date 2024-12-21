package com.pmi.tpd.web.rest.rsrc.api.scheduling;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.scheduler.CountingJobRunner;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint for view and managing Log Level at runtime.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Path(RestApplication.API_RESOURCE_PATH + "/scheduler")
@Tag(description = "Endpoint for scheduler", name = "scheduler")
public class SchedulerResource {

    private final CountingJobRunner countingJobRunner;

    @Inject
    public SchedulerResource(final CountingJobRunner countingJobRunner) {
        this.countingJobRunner = countingJobRunner;
    }

    /**
     * @return Returns list of last log.
     */
    @Path("{jobPrefix}/{runMode}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @RolesAllowed({ ApplicationConstants.Authorities.ANONYMOUS })
    @Operation(summary = "Gets count of job")
    public Response countJob(@PathParam("jobPrefix") final String jobPrefix,
        @PathParam("runMode") final RunMode runMode,
        @QueryParam("timeout") @DefaultValue("150") final long timeout,
        @QueryParam("unit") @DefaultValue("SECONDS") final TimeUnit unit) {

        final int count = countingJobRunner.awaitCount(jobPrefix, runMode, timeout, unit);

        return ResponseFactory.ok(Collections.singletonMap("count", count)).build();
    }

}
