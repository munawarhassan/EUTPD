package com.pmi.tpd.web.rest.rsrc.api;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.common.Strings;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.codahale.metrics.annotation.Timed;
import com.pmi.tpd.api.audit.IAuditEvent;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.audit.IAuditEventService;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST endpoint for getting the audit events.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Path(RestApplication.API_RESOURCE_PATH + "/audits")
@Tag(description = "Endpoint for getting the audit events", name = "audit")
public class AuditResource {

    /** */
    private static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    /** */
    private final IAuditEventService auditEventService;

    /**
     * Default Constructor.
     *
     * @param auditEventService
     *                          audit event service
     */
    @Inject
    public AuditResource(@Nonnull final IAuditEventService auditEventService) {
        this.auditEventService = Assert.notNull(auditEventService);
    }

    /**
     * @param pageable
     *                 defines the page of audit to retrieve.
     * @return Returns the requested page of {@link IAuditEvent}, potentially filtered, which may be empty but never
     *         {@code null}.
     */
    @GET
    @Path("all")
    @PreAuthorize("isAuthenticated()")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Finds all audit events")
    public Response findAll(@Context final Pageable pageable) {
        return ResponseFactory.ok(auditEventService.findAll(pageable)).build();
    }

    /**
     * Finds all audit events by range date and associated to specific channels.
     *
     * @param pageable
     *                 defines the page of audit to retrieve.
     * @param fromDate
     *                 the from date.
     * @param toDate
     *                 the from date.
     * @return Returns the requested page of {@link IAuditEvent} filtered from {@code fromDate} to {@code toDate}, which
     *         may be empty but never {@code null}.
     */
    @GET
    @Path("byDates")
    @Produces(MediaType.APPLICATION_JSON)
    @PreAuthorize("isAuthenticated()")
    @Timed
    @Operation(summary = "Finds all audit events by range date",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "200", description = "The request has succeeded") })
    public Response findByDates(@Context final Pageable pageable,
        @Parameter(required = false, description = "from date (yyyy-MM-dd)") //
        @QueryParam("fromDate") final String fromDate, //
        @Parameter(required = false, description = "to date (yyyy-MM-dd)") //
        @QueryParam("toDate") final String toDate,
        @Parameter(required = false, description = "representing string[], separator ','") //
        @QueryParam("channels") final String channels) {
        String[] cs = ArrayUtils.EMPTY_STRING_ARRAY;
        if (!Strings.isNullOrEmpty(channels)) {
            cs = channels.split(",");
        }

        return ResponseFactory.ok(auditEventService.findByDates(pageable, toDate(fromDate), toDate(toDate), cs))
                .build();
    }

    private static LocalDate toDate(final String text) {
        if (Strings.isNullOrEmpty(text)) {
            return null;
        }
        return formatter.parseLocalDate(text);
    }
}
