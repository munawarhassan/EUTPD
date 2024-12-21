package com.pmi.tpd.euceg.backend.core.api;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.data.domain.Pageable;

import com.github.hanleyt.JerseyExtension;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.util.RandomUtil;
import com.pmi.tpd.euceg.backend.core.domibus.api.DomibusResource;
import com.pmi.tpd.euceg.backend.core.domibus.api.IClientRest;
import com.pmi.tpd.euceg.backend.core.domibus.api.model.MessageLog;
import com.pmi.tpd.euceg.backend.core.domibus.api.model.MessageLogResponse;
import com.pmi.tpd.testing.junit5.MockitoTestCase;
import com.pmi.tpd.web.core.rs.container.PageableFactory;

public class DomibusResourceTest extends MockitoTestCase {

    private IClientRest clientRest;

    @RegisterExtension
    public JerseyExtension jerseyExtension = new JerseyExtension(this::configureJersey);

    private Application configureJersey(final ExtensionContext extensionContext) {
        clientRest = mock(IClientRest.class, withSettings().lenient());
        final ResourceConfig resource = new ResourceConfig().register(new DomibusResource(clientRest))
                .register(new AbstractBinder() {

                    @Override
                    protected void configure() {
                        bindFactory(PageableFactory.class).to(Pageable.class);
                    }
                });
        return resource;
    }

    @Test
    public void shouldReturnMessageLogs(final WebTarget target) {
        final String uuid = RandomUtil.uuid();
        final MessageLogResponse logResponse = MessageLogResponse.builder()
                .messageLogEntries(Lists.newArrayList(MessageLog.builder().build()))
                .build();
        when(clientRest.getMessageLogs(eq(uuid), any())).thenReturn(logResponse);
        final Response response = target.path(DomibusResource.API_PATH + "/" + uuid + "/messageLogs").request().get();
        assertNotNull(response);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
    }

}
