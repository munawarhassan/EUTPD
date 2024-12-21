package com.pmi.tpd.web.rest.rsrc.api.setup;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.mvc.Template;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.core.IGlobalApplicationProperties;
import com.pmi.tpd.web.rest.RestApplication;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Path(RestApplication.API_RESOURCE_PATH + ErrorSetupResource.PAGE_NAME)
public class ErrorSetupResource {

    /** */
    public static final String PAGE_NAME = "/setupError.html";

    /** */
    public static final String FULL_PAGE_NAME = RestApplication.API_RESOURCE_PATH + PAGE_NAME;

    /** */
    private final IGlobalApplicationProperties globalApplicationProperties;

    private final IEventAdvisorService<?> eventAdvisorService;

    /** */
    @Context
    private UriInfo uriInfo;

    /**
     * @param globalApplicationProperties
     * @param eventService
     */
    @Inject
    public ErrorSetupResource(final IGlobalApplicationProperties globalApplicationProperties,
            final IEventAdvisorService<ServletContext> eventAdvisorService) {
        this.globalApplicationProperties = checkNotNull(globalApplicationProperties, "globalApplicationProperties");
        this.eventAdvisorService = checkNotNull(eventAdvisorService, "eventAdvisorService");
    }

    /**
     * @return
     */
    @GET
    @Template(name = "/setup/error.mustache")
    @Produces("text/html")
    public Response error() {
        final IEventContainer eventContainer = eventAdvisorService.getEventContainer();
        if (!eventContainer.hasEvents()) {
            return Response.seeOther(uriInfo.getBaseUri()).build();
        }
        final Collection<Event> events = eventContainer.getEvents();
        final Map<String, Object> data = ImmutableMap.<String, Object> builder()
                .put("applicationName", globalApplicationProperties.getDisplayName())
                .put("events", events)
                .build();
        return Response.ok(data).type(MediaType.TEXT_HTML_TYPE).build();
    }
}
