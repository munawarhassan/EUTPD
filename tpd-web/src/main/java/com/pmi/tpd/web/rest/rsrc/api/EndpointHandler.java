package com.pmi.tpd.web.rest.rsrc.api;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.springframework.context.ApplicationContext;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.web.core.rs.endpoint.Endpoint;
import com.pmi.tpd.web.core.rs.endpoint.RestEndpoint;
import com.pmi.tpd.web.core.rs.endpoint.RestEndpoints;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Path("endpoint")
@Tag(description = "Endpoint for metrics, health check  monitoring", name = "endpoint")
public class EndpointHandler {

    /** */
    private final RestEndpoints endpoints;

    /**
     * Create a new {@link EndpointHandler} instance. All {@link Endpoint}s will be detected from the
     * {@link ApplicationContext}.
     *
     * @param endpoints
     *                  the endpoints
     */
    @Inject
    public EndpointHandler(final RestEndpoints endpoints) {
        this.endpoints = Assert.notNull(endpoints);
    }

    /**
     * @param name
     * @param request
     * @param context
     * @return
     */
    @GET
    @Path("{name}")
    @PermitAll()
    @Produces(MediaType.APPLICATION_JSON)
    @Schema(allowableValues = { "health" }, defaultValue = "health")
    @Operation(description = "execute specific endpoint")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "The request has succeeded"),
            @ApiResponse(responseCode = "400", description = "the endpoint name is required") })
    public Response invoke(@Parameter(required = true) @PathParam("name") final String name,
        @Context final Request request,
        @Context final SecurityContext context) {
        final RestEndpoint endpoint = endpoints.findEndpoint(name);
        if (endpoint == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        final Object value = endpoint.invoke(request, context);
        if (value instanceof Response) {
            return (Response) value;
        }
        return Response.ok(value).build();
    }
}
