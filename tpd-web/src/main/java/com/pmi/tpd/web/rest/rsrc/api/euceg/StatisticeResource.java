package com.pmi.tpd.web.rest.rsrc.api.euceg;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.data.domain.Pageable;

import com.codahale.metrics.annotation.Timed;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.paging.Filters;
import com.pmi.tpd.api.paging.IFilterable;
import com.pmi.tpd.core.euceg.stat.CountResult;
import com.pmi.tpd.core.euceg.stat.HistogramRequest;
import com.pmi.tpd.core.euceg.stat.HistogramResult;
import com.pmi.tpd.core.euceg.stat.IEucegStatisticService;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Named
@Path(RestApplication.API_RESOURCE_PATH + "/euceg/stat")
@Tag(description = "Endpoint for Product submission", name = "statistic")
public class StatisticeResource {

    private final IEucegStatisticService eucegStatisticService;

    @Inject
    public StatisticeResource(final IEucegStatisticService eucegStatisticService) {
        this.eucegStatisticService = eucegStatisticService;
    }

    @GET
    @Path("submission/count/byStatus")
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Count of submissions",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = CountResult.class))) })
    public Response countSubmission() {
        return ResponseFactory.ok(this.eucegStatisticService.countSubmissionByStatus()).build();
    }

    @POST
    @Path("submission/recent")
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "histogram of Recent submissions",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = HistogramResult.class))) })
    public Response histogramRecentSubmission(@Valid final HistogramRequest request) {
        return ResponseFactory.ok(this.eucegStatisticService.getHistogramCreatedSubmission(request)).build();
    }

    @GET
    @Path("product/{productType}/count/byPirStatus")
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Count of products by PIR Status",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = CountResult.class))) })
    public Response countTobaccoProductByPirStatus(
        @NotNull @Nonnull @PathParam("productType") final ProductType productType) {
        return ResponseFactory.ok(this.eucegStatisticService.countProductByPirStatus(productType)).build();
    }

    @GET
    @Path("product/count/bySubmissionType")
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Count of products by Submission Type",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = CountResult.class))) })
    public Response countEcigProductBySubmission(@Context final Pageable pageRequest) {
        Filters filters = null;
        final String query = null;
        if (pageRequest instanceof IFilterable) {
            filters = ((IFilterable) pageRequest).getFilters();
        }
        return ResponseFactory.ok(this.eucegStatisticService.countProductBySubmissionType(filters, query)).build();
    }

    @POST
    @Path("product/tobacco/recent")
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "histogram of recent tobacoo product",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = HistogramResult.class))) })
    public Response histogramRecentTobaccoProduct(@Valid final HistogramRequest request) {
        return ResponseFactory.ok(this.eucegStatisticService.getHistogramCreatedTobaccoProduct(request)).build();
    }

    @POST
    @Path("product/ecig/recent")
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "histogram of recent ecig product",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = HistogramResult.class))) })
    public Response histogramRecentEcigProduct(@Valid final HistogramRequest request) {
        return ResponseFactory.ok(this.eucegStatisticService.getHistogramCreatedEcigProduct(request)).build();
    }

    @GET
    @Path("attachment/count/byStatus")
    @RolesAllowed(ApplicationConstants.Authorities.USER)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(summary = "Count of attachments by status",
            responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = CountResult.class))) })
    public Response countAttachment() {
        return ResponseFactory.ok(this.eucegStatisticService.countAttachmentByStatus()).build();
    }

}
