package com.pmi.tpd.web.rest.rsrc.api;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.pmi.tpd.api.Product;
import com.pmi.tpd.web.rest.RestApplication;

import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Path("/openapi.{type:json|yaml}")
public class OpenApiResource extends BaseOpenApiResource {

    private final ServletConfig config;

    private final Application app;

    public OpenApiResource(@Context final ServletConfig config, @Context final Application app) {

        this.config = config;
        this.app = app;
    }

    private void init(final UriInfo uriInfo) {
        // SecurityScheme api-key
        final SecurityScheme basicAuth = new SecurityScheme();
        basicAuth.setName("basicAuth");
        basicAuth.setType(SecurityScheme.Type.APIKEY);
        basicAuth.setIn(SecurityScheme.In.HEADER);
        final SecurityScheme bearerAuth = new SecurityScheme();
        bearerAuth.setName("bearerAuth");
        bearerAuth.setType(SecurityScheme.Type.APIKEY);
        bearerAuth.setIn(SecurityScheme.In.HEADER);

        final OpenAPI oas = new OpenAPI();
        final Info info = new Info().version(RestApplication.API_VERSION)
                .title(Product.getFullName() + " API Documentation");
        oas.info(info);
        oas.schemaRequirement(basicAuth.getName(), basicAuth);
        oas.schemaRequirement(bearerAuth.getName(), bearerAuth);
        final Server serverOpenApi = new Server();
        serverOpenApi.setUrl(uriInfo.getBaseUri().toString());
        serverOpenApi.setDescription("Backend TPD Server");
        oas.addServersItem(serverOpenApi);

        setOpenApiConfiguration(new SwaggerConfiguration().openAPI(oas)
                .prettyPrint(true)
                .resourcePackages(Stream.of(UsersResource.class.getPackageName()).collect(Collectors.toSet())));
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON, "application/yaml" })
    @Operation(hidden = true)
    public Response getOpenApi(@Context final HttpHeaders headers,
        @Context final UriInfo uriInfo,
        @PathParam("type") final String type) throws Exception {
        this.init(uriInfo);
        return super.getOpenApi(headers, config, app, uriInfo, type);
    }

}
