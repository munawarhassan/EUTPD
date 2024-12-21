package com.pmi.tpd.euceg.backend.core.domibus.api;

import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.euceg.backend.core.domibus.api.model.ErrorLogResponse;
import com.pmi.tpd.euceg.backend.core.domibus.api.model.MessageLogResponse;

@Path(DomibusResource.API_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DomibusResource {

    public static final String API_PATH = "api/domibus";

    private final IClientRest client;

    @Inject
    public DomibusResource(@Nonnull final IClientRest client) {
        this.client = Assert.checkNotNull(client, "client");
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{conversationId}/messageLogs")
    public Response getMessageLogs(@PathParam("conversationId") final String conversationId,
        @Context final Pageable pageRequest) {
        final MessageLogResponse resp = this.client.getMessageLogs(conversationId, pageRequest);
        return Response.ok(PageUtils.createPage(
            resp.getMessageLogEntries() != null ? resp.getMessageLogEntries() : Collections.emptyList(),
            pageRequest)).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("{messageId}/errorLogs")
    public Response getErrorLogs(@PathParam("messageId") final String messageId, @Context final Pageable pageRequest) {
        final ErrorLogResponse resp = this.client.getErrorLogs(messageId, pageRequest);
        return Response.ok(PageUtils.createPage(
            resp.getErrorLogEntries() != null ? resp.getErrorLogEntries() : Collections.emptyList(),
            pageRequest)).build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("pmode/current")
    public Response getCurrentPMode() {
        return Response.ok(this.client.getCurrentPMode().getEntity()).build();
    }
}
