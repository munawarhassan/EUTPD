package com.pmi.tpd.web.rest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.wadl.WadlFeature;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.MetricsFeature;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.euceg.backend.core.domibus.api.DomibusResource;
import com.pmi.tpd.web.core.rs.container.HttpSessionFactory;
import com.pmi.tpd.web.core.rs.container.LoggingFilter;
import com.pmi.tpd.web.core.rs.container.PageableFactory;
import com.pmi.tpd.web.core.rs.container.RequestFilteringFeature;
import com.pmi.tpd.web.core.rs.container.SecurityFeature;
import com.pmi.tpd.web.core.rs.jackson.CustomJacksonFeature;
import com.pmi.tpd.web.core.rs.jackson.internal.JacksonObjectMapperProvider;
import com.pmi.tpd.web.rest.rsrc.api.ApplicationStatusResource;
import com.pmi.tpd.web.rest.rsrc.api.AuditResource;
import com.pmi.tpd.web.rest.rsrc.api.EndpointHandler;
import com.pmi.tpd.web.rest.rsrc.api.InfoResource;
import com.pmi.tpd.web.rest.rsrc.api.LogsResource;
import com.pmi.tpd.web.rest.rsrc.api.OpenApiResource;
import com.pmi.tpd.web.rest.rsrc.api.SecurityResource;
import com.pmi.tpd.web.rest.rsrc.api.UsersResource;
import com.pmi.tpd.web.rest.rsrc.api.admin.ConfigurationResource;
import com.pmi.tpd.web.rest.rsrc.api.admin.GlobalPermissionResource;
import com.pmi.tpd.web.rest.rsrc.api.admin.KeyStoreResource;
import com.pmi.tpd.web.rest.rsrc.api.admin.MaintenanceResource;
import com.pmi.tpd.web.rest.rsrc.api.admin.UserAdminResource;
import com.pmi.tpd.web.rest.rsrc.api.auth.JwtResource;
import com.pmi.tpd.web.rest.rsrc.api.euceg.AttachmentResource;
import com.pmi.tpd.web.rest.rsrc.api.euceg.ProductResource;
import com.pmi.tpd.web.rest.rsrc.api.euceg.ReferenceResource;
import com.pmi.tpd.web.rest.rsrc.api.euceg.StatisticeResource;
import com.pmi.tpd.web.rest.rsrc.api.euceg.SubmissionProductResource;
import com.pmi.tpd.web.rest.rsrc.api.euceg.SubmissionReportingResource;
import com.pmi.tpd.web.rest.rsrc.api.euceg.SubmitterResource;
import com.pmi.tpd.web.rest.rsrc.api.scheduling.SchedulerResource;
import com.pmi.tpd.web.rest.rsrc.api.setup.ErrorSetupResource;
import com.pmi.tpd.web.rest.rsrc.api.setup.SetupResource;

import io.swagger.v3.jaxrs2.SwaggerSerializers;

/**
 * @author Christophe Friederich
 */
@Named("RestApplication")
@ApplicationPath("/")
public class RestApplication extends ResourceConfig {

    /** */
    public static final String BASE_RESOURCE_PATH = "rest";

    /** */
    public static final String API_RESOURCE_PATH = "api";

    /** */
    public static final String FULL_REST_API_RESOURCE_PATH = BASE_RESOURCE_PATH + "/" + API_RESOURCE_PATH;

    /** */
    public static final String API_VERSION = "1.0";

    /** */
    public static final String API_PATH = API_RESOURCE_PATH + "/" + API_VERSION;

    /**
     *
     */
    @Inject
    public RestApplication(@Context ServletConfig servletConfig, final MetricRegistry metricRegistry) {
        Assert.checkNotNull(metricRegistry, "metricRegistry");

        register(JwtResource.class);

        // API regitration
        register(MaintenanceResource.class);
        register(InfoResource.class);
        register(LogsResource.class);
        register(SchedulerResource.class);
        register(SecurityResource.class);
        register(AuditResource.class);
        register(ConfigurationResource.class);
        register(UsersResource.class);
        register(ApplicationStatusResource.class);
        register(KeyStoreResource.class);
        register(UserAdminResource.class);
        register(GlobalPermissionResource.class);

        register(ReferenceResource.class);
        register(SubmitterResource.class);
        register(SubmissionProductResource.class);
        register(SubmissionReportingResource.class);
        register(ProductResource.class);
        register(AttachmentResource.class);
        register(DomibusResource.class);
        register(StatisticeResource.class);

        register(EndpointHandler.class);

        register(SetupResource.class);
        register(ErrorSetupResource.class);

        register(SwaggerSerializers.class);

        // OpenApiResource openApiResource = new OpenApiResource(servletConfig, this);
        register(OpenApiResource.class);

        register(HandlingErrorFeature.class);
        register(JacksonObjectMapperProvider.class);
        register(CustomJacksonFeature.class);

        register(SecurityFeature.class);
        register(RequestFilteringFeature.class);
        register(WadlFeature.class);
        // enable multipart
        register(MultiPartFeature.class);

        register(new AbstractBinder() {

            @Override
            protected void configure() {
                bindFactory(HttpSessionFactory.class).to(HttpSession.class)
                        .proxy(true)
                        .proxyForSameScope(false)
                        .in(RequestScoped.class);
                bindFactory(PageableFactory.class).to(Pageable.class);
            }
        });

        /*
         * Metrics Config
         */
        register(new MetricsFeature(metricRegistry));

        // Enable LoggingFilter & output entity.
        registerInstances(new LoggingFilter(LoggerFactory.getLogger(RestApplication.class.getName()), true));

    }

}
