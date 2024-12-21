package com.pmi.tpd.web.rest.rsrc.api.admin;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.data.domain.Pageable;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Iterables;
import com.google.common.io.Closeables;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.crypto.KeyPair;
import com.pmi.tpd.api.crypto.KeyPairHelper;
import com.pmi.tpd.api.crypto.X509CertHelper;
import com.pmi.tpd.api.exception.ArgumentValidationException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.keystore.IKeyStoreService;
import com.pmi.tpd.keystore.KeyStoreProperties;
import com.pmi.tpd.keystore.model.KeyStoreEntry;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.model.KeyStoreSetting;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Path(RestApplication.API_RESOURCE_PATH + "/keystores")
@Consumes(MediaType.APPLICATION_JSON)
@Tag(description = "Endpoint for KeyStore managment", name = "administration")
public class KeyStoreResource {

  /** */
  // private static final Logger LOGGER =
  // LoggerFactory.getLogger(KeyStoreResource.class);

  /** */
  private final IKeyStoreService keyStoreService;

  /** */
  private final IApplicationProperties applicationProperties;

  /** */
  private final I18nService i18nService;

  /**
   * @param keyStoreService
   * @param applicationProperties
   * @param i18nService
   */
  @Inject
  public KeyStoreResource(@Nonnull final IKeyStoreService keyStoreService,
      @Nonnull final IApplicationProperties applicationProperties, @Nonnull final I18nService i18nService) {
    this.keyStoreService = checkNotNull(keyStoreService, "keyStoreService");
    this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
    this.i18nService = checkNotNull(i18nService, "i18nService");
  }

  @GET
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN, ApplicationConstants.Authorities.ADMIN })
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @Operation(summary = "Gets all keystore entries")
  public Response findAll(
      @Parameter(description = "page to load (zero-based page index)", required = false) @QueryParam("page") @DefaultValue("0") final int page,
      @Parameter(description = "size of page", required = false) @QueryParam("size") @DefaultValue("20") final int size,
      @Parameter(description = "sort of page", required = false) @QueryParam("sort") final String sort,
      @Parameter(description = "filtering of page", required = false) @QueryParam("filter") final String filter)
      throws Exception {
    final Pageable pageRequest = PageUtils.newRequest(page, size, sort, filter, null);
    return Response.ok(this.keyStoreService.findAll(pageRequest)).build();
  }

  @Timed
  @Path("{alias}")
  @GET
  @Operation(summary = "get a key", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = KeyStoreEntry.class))),
      @ApiResponse(responseCode = "400", description = "The request was malformed."),
      @ApiResponse(responseCode = "401", description = "The authenticated user does not have the SYS_ADMIN permission."),
      @ApiResponse(responseCode = "404", description = "The specified key does not exist."),
      @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
          + " which prevented it from fulfilling the request.")
  })
  @RolesAllowed(ApplicationConstants.Authorities.SYS_ADMIN)
  public Response get(@Parameter(description = "key to get", required = true) @PathParam("alias") final String alias) {
    if (StringUtils.isEmpty(alias)) {
      throw new ArgumentValidationException(i18nService.createKeyedMessage("app.service.keystore.get.no.alias"));
    }
    final KeyStoreEntry entry = this.keyStoreService.get(alias);
    if (entry == null) {
      return ResponseFactory.notFound().build();
    }
    return ResponseFactory.ok(this.keyStoreService.get(alias)).build();
  }

  @Timed
  @Path("{alias}")
  @DELETE
  @Operation(summary = "delelete a key", responses = {
      @ApiResponse(responseCode = "200", description = "The request has succeeded"),
      @ApiResponse(responseCode = "400", description = "The request was malformed."),
      @ApiResponse(responseCode = "401", description = "The authenticated user does not have the SYS_ADMIN permission."),
      @ApiResponse(responseCode = "404", description = "The specified key does not exist."),
      @ApiResponse(responseCode = "500", description = "The server encountered an  unexpected condition"
          + " which prevented it from fulfilling the request.") })
  @RolesAllowed(ApplicationConstants.Authorities.SYS_ADMIN)
  public Response deleteAlias(
      @Parameter(description = "key to delete", required = true) @PathParam("alias") final String alias) {
    if (StringUtils.isEmpty(alias)) {
      throw new ArgumentValidationException(
          i18nService.createKeyedMessage("app.service.keystore.delete.no.alias"));
    }

    this.keyStoreService.remove(alias);
    return ResponseFactory.ok().build();
  }

  /**
   * @param file
   * @return
   * @throws Exception
   */
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Path("certificate/validate")
  @POST
  @Timed
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response validateTrutedCertificate(@FormDataParam("file") final InputStream file) throws Exception {
    try {
      final List<X509Certificate> certs = X509CertHelper.loadCertificates(file);
      final X509Certificate certificate = Iterables.getFirst(certs, null);
      return Response.ok(X509CertHelper.getCertificateAlias(certificate)).build();
    } finally {
      Closeables.closeQuietly(file);
    }
  }

  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Path("certificate")
  @POST
  @Timed
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response importTrutedCertificate(@FormDataParam("alias") final String alias,
      @FormDataParam("file") final InputStream file) throws Exception {
    Assert.checkHasText(alias, "alias");
    final List<X509Certificate> certs = X509CertHelper.loadCertificates(file);
    final X509Certificate certificate = Iterables.getFirst(certs, null);
    this.keyStoreService.storeCertificate(certificate, alias);
    return Response.accepted().build();
  }

  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Path("keypair/validate")
  @POST
  @Timed
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response validateKeyPair(@FormDataParam("password") final String password,
      @FormDataParam("file") final InputStream keypairFile) throws Exception {
    Assert.checkHasText(password, "password");
    return ResponseFactory.ok(new AliasReponse(this.keyStoreService.validateKeyPair(keypairFile, password)))
        .build();

  }

  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Path("keypair")
  @POST
  @Timed
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response importKeyPair(@FormDataParam("alias") final String alias,
      @FormDataParam("password") final String password,
      @FormDataParam("file") final InputStream file) throws Exception {
    Assert.checkHasText(alias, "alias");
    final KeyPair keypair = KeyPairHelper.extractKeyPairPkcs12(file, password);
    this.keyStoreService.storeKey(keypair, alias);
    return Response.accepted().build();
  }

  @GET
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @Operation(summary = "Gets keystore settings", responses = {
      @ApiResponse(content = @Content(schema = @Schema(implementation = KeyStoreSetting.class))) })
  @Path("settings")
  public Response getSettings() throws Exception {
    final KeyStoreProperties configuration = this.keyStoreService.getConfiguration();
    final KeyStoreSetting setting = KeyStoreSetting.create(configuration);
    return ResponseFactory.ok(setting).build();
  }

  @POST
  @RolesAllowed({ ApplicationConstants.Authorities.SYS_ADMIN })
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @Operation(summary = "Update keystore settings")
  @Path("settings")
  public Response updateSettings(@Valid final KeyStoreSetting setting) throws Exception {
    setting.save(applicationProperties);
    return ResponseFactory.accepted().build();
  }

  @Data
  @RequiredArgsConstructor
  private class AliasReponse {

    private final String alias;
  }
}
