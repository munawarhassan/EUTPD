package com.pmi.tpd.web.rest.rsrc.api;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Timed;
import com.pmi.tpd.api.versioning.IBuildUtilsInfo;
import com.pmi.tpd.core.IGlobalApplicationProperties;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.model.GitRepositoryState;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Path(RestApplication.API_RESOURCE_PATH + "/info")
@Tag(description = "Endpoint for Application information", name = "information")
@Component
public class InfoResource {

    /** */
    @Inject
    private IBuildUtilsInfo info;

    /** */
    @Inject
    private IGlobalApplicationProperties global;

    @GET
    @PermitAll
    @PreAuthorize("isAuthenticated()")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Gets global application information", responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = IGlobalApplicationProperties.class))) })
    public Response info() {
        return ResponseFactory.ok(global).build();
    }

    /**
     * @return
     */
    @GET
    @Path("scm")
    @PermitAll
    @PreAuthorize("isAuthenticated()")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Gets scm information", responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = GitRepositoryState.class))) })
    public Response scm() {
        return ResponseFactory.ok(new GitRepositoryState(info.getBuildProperties())).build();
    }

}
