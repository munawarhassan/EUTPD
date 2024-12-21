package com.pmi.tpd.web.rest.rsrc.api.euceg;

import java.io.IOException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import com.codahale.metrics.annotation.Timed;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.avatar.INavBuilder;
import com.pmi.tpd.core.euceg.report.ISubmissionReportTrackingService;
import com.pmi.tpd.euceg.core.exporter.submission.SubmissionReportType;
import com.pmi.tpd.scheduler.exec.ITaskMonitor;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.model.ReportRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Path(RestApplication.API_RESOURCE_PATH + "/submissions/reporting")
@Tag(description = "Endpoint for Submission Reporting", name = "business")
@Named()
public class SubmissionReportingResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionReportingResource.class);

    private final ISubmissionReportTrackingService reportTrackingManager;

    private final INavBuilder navBuilder;

    @Inject
    public SubmissionReportingResource(final ISubmissionReportTrackingService reportTrackingManager,
            final INavBuilder navBuilder) {
        this.reportTrackingManager = Assert.checkNotNull(reportTrackingManager, "reportTrackingManager");
        this.navBuilder = Assert.checkNotNull(navBuilder, "navBuilder");

    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("reports")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Timed
    @Operation(summary = "Gets list of Reports",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = CollectionModel.class))) })
    public Response findAll(
        @Parameter(description = "page to load (zero-based page index)",
                required = false) @QueryParam("page") @DefaultValue("0") final int page,
        @Parameter(description = "size of page",
                required = false) @QueryParam("size") @DefaultValue("20") final int size,
        @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
        @Parameter(description = "filters product", required = false) @QueryParam("filter") final String filter,
        @Parameter(description = "search report with expression",
                required = false) @QueryParam("search") final String search) {
        final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, search);
        return ResponseFactory
                .ok(this.reportTrackingManager.findAll(pageRequest)
                        .map(e -> ReportRequest.from(e,
                            navBuilder.builder("rest", "api", "submissions", "reporting", "reports", e.getName()))))
                .build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("reports/{filename}")
    @Timed
    @Produces(MediaType.WILDCARD)
    @Operation(summary = "Gets a report file",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = EntityModel.class))) })
    public Response get(@PathParam("filename") @NotEmpty final String filename) throws IOException {
        try {
            final var report = this.reportTrackingManager.getByName(filename);
            return ResponseFactory.ok(report.openStream())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + report.getName() + "\"")
                    .type(MediaType.valueOf(report.getContentType()))
                    .build();
        } catch (final NoSuchEntityException | EntityNotFoundException e) {
            return ResponseFactory.notFound().build();
        }
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Path("reports/{filename}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "delete a report")
    public Response delete(@Parameter(description = "report to delete",
            required = true) @PathParam("filename") @NotEmpty final String filename) {
        this.reportTrackingManager.delete(filename);
        return ResponseFactory.ok().build();
    }

    /**
     * Generate report containing list of submission in excel file.
     *
     * @param productType
     *                       the product type.
     * @param productNumbers
     *                       the list of product numbers.
     * @return Returns a generate excel file containing products associated to.
     * @since 2.5
     */
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @POST
    @Path("{reportType}/report")
    @Timed
    @Operation(summary = "Generate report containing list of submission", parameters = {
            @Parameter(name = "page", schema = @Schema(type = "integer"),
                    description = "page to load (zero-based page index)", required = false, in = ParameterIn.QUERY),
            @Parameter(name = "size", schema = @Schema(type = "integer"), description = "size of page",
                    required = false, in = ParameterIn.QUERY),
            @Parameter(name = "sort", schema = @Schema(type = "integer"), description = "sort of page",
                    required = false, in = ParameterIn.QUERY),
            @Parameter(name = "filter", schema = @Schema(type = "string",
                    example = "[property]::[eq,noteq,contains,start,end,lte,gte,exists,before,after,between,in,notin]==[value1,value2]|[property]..."),
                    description = "filters", required = false, in = ParameterIn.QUERY,
                    examples = { @ExampleObject(
                            value = "[property]::[eq,noteq,contains,start,end,lte,gte,exists,before,after,between,in,notin]==[value1,value2]|[property]...",
                            summary = "Structure a filter"),
                            @ExampleObject(value = "submissionStatus::eq==SUBMITTED",
                                    summary = "Filter all submitted submissions"),
                            @ExampleObject(
                                    value = "submissionStatus::eq==SUBMITTED|lastModifiedDate::between==2023-05-01T00:00:00.000Z,2023-05-31T21:59:59.999Z",
                                    summary = "Filter all submitted submissiosn for the month of May") }) })
    public Response generateReport(@PathParam("reportType") final SubmissionReportType reportName,
        @Context final Pageable pageable,
        @QueryParam("limit") @DefaultValue("10000") final long limit) {

        final ITaskMonitor taskMonitor = this.reportTrackingManager.trackingReport(reportName, pageable, limit);

        return ResponseFactory.ok(taskMonitor).build();

    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("cancel/{token}")
    @Timed
    @Operation(summary = "cancel task execution")
    public Response cancelReport(@PathParam("token") final String token) {

        this.reportTrackingManager.cancelTask(token);
        return ResponseFactory.accepted().build();
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("progress/{token}")
    @Operation(summary = "Returns progress generation report execution")
    public Response progressReport(@PathParam("token") final String token) {
        return reportTrackingManager.getTaskMonitor(token)
                .map(t -> ResponseFactory.ok(t.getProgress()).build())
                .orElse(ResponseFactory.notFound().build());
    }

    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @GET
    @Path("file/latest")
    @Operation(summary = "Returns the latest excel tracking file")
    public Response getLatestReport() {
        return this.reportTrackingManager.findLatest().map(report -> {
            final StreamingOutput outputStream = output -> report.asByteSource().copyTo(output);
            return ResponseFactory.ok(outputStream)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.getName() + "\"")
                    .type(MediaType.valueOf("application/vnd.ms-excel"))
                    .build();
        }).orElse(ResponseFactory.notFound().build());

    }

}
