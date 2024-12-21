package com.pmi.tpd.euceg.backend.core.domibus.ws;

import java.nio.file.Paths;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;

import org.apache.cxf.endpoint.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.lifecycle.ConfigurationChangedEvent;
import com.pmi.tpd.euceg.api.BackendNotStartedException;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.BackendProperties.ConnectionType;
import com.pmi.tpd.euceg.backend.core.ISenderMessageCreator;
import com.pmi.tpd.euceg.backend.core.TestEventPublisher;
import com.pmi.tpd.euceg.backend.core.event.EventBackendReceived;
import com.pmi.tpd.euceg.backend.core.message.IBackendMessage;
import com.pmi.tpd.euceg.backend.core.message.Response;
import com.pmi.tpd.euceg.backend.core.message.SubmitResponse;
import com.pmi.tpd.euceg.backend.core.spi.IPendingMessageProvider;
import com.pmi.tpd.euceg.backend.core.support.ByteArrayDataSource;
import com.pmi.tpd.euceg.backend.core.support.SimpleSenderMessageCreator;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

import eu.domibus.backend.ws.BackendInterface;
import eu.domibus.backend.ws.ErrorResultImplArray;
import eu.domibus.backend.ws.LargePayloadType;
import eu.domibus.backend.ws.ListPendingMessagesResponse;
import eu.domibus.backend.ws.MessageStatus;
import eu.domibus.backend.ws.RetrieveMessageFault;
import eu.domibus.backend.ws.RetrieveMessageResponse;
import eu.domibus.backend.ws.StatusFault;
import eu.domibus.backend.ws.message.CollaborationInfo;
import eu.domibus.backend.ws.message.Messaging;
import eu.domibus.backend.ws.message.UserMessage;

public class WsMessageSenderTest extends MockitoTestCase {

    @Mock(lenient = true)
    private IPendingMessageProvider pendingMessageProvider;

    private final ISenderMessageCreator<String, String> senderMessageCreator = new SimpleSenderMessageCreator();

    @Spy
    private final SimpleI18nService i18nService = new SimpleI18nService();

    private final TestEventPublisher<EventBackendReceived<IBackendMessage>> eventPublisher = new TestEventPublisher<>();

    @Mock(lenient = true)
    private IApplicationProperties applicationProperties;

    private WsMessageSender<String, String> sender;

    @Mock(lenient = true)
    private BackendInterfaceClient backendInterface;

    private final BackendProperties backendProperties = new BackendProperties();

    @BeforeEach
    public void setUp() throws Exception {
        sender = new WsMessageSender<>(senderMessageCreator, pendingMessageProvider, i18nService, applicationProperties,
                eventPublisher);
        sender.setBackendInterface(backendInterface);
        when(applicationProperties.getConfiguration(eq(BackendProperties.class))).thenReturn(backendProperties);

        when(backendInterface.listPendingMessages(any()))
                .thenReturn(new ListPendingMessagesResponse().withMessageID(Lists.newArrayList()));
        when(pendingMessageProvider.getPendingMessageIds()).thenReturn(Sets.newHashSet());
    }

    @AfterEach
    public void teardown() {
        if (sender.isRunning()) {
            sender.shutdown();
        }
    }

    @Test
    public void testCheckStarted() throws Exception {
        Assertions.assertThrows(BackendNotStartedException.class, () -> sender.checkStarted());

    }

    @Test
    public void shouldNotStartWhenEmptyProperties() throws Exception {
        sender.start();
        assertEquals(false, sender.isRunning());
    }

    @Test
    public void shouldStartAndShutdown() throws Exception {
        getOrCreateConfiguration(backendProperties);
        sender.start();
        assertEquals(true, sender.isRunning());
        sender.shutdown();
        assertEquals(false, sender.isRunning());
        verify(backendInterface, times(1)).destroy();

    }

    @Test
    public void shouldStartAndShutdownOnFailed() throws Exception {
        getOrCreateConfiguration(backendProperties);
        doThrow(new RuntimeException()).when(backendInterface).destroy();
        sender.start();
        sender.shutdown();
        assertEquals(false, sender.isRunning());
    }

    @Test
    public void shouldRestartWhenUpdateConfiguration() throws Exception {
        getOrCreateConfiguration(backendProperties);
        sender.start();
        assertEquals("http://service.com/services/backend", sender.getBackendProperties().getWebServiceUrl());

        final BackendProperties newConfiguration = getNewConfiguration();
        when(applicationProperties.getConfiguration(eq(BackendProperties.class))).thenReturn(newConfiguration);
        sender.onDomibusConfigurationChangedEvent(new ConfigurationChangedEvent<>(newConfiguration));

        assertEquals(true, sender.isRunning());
        assertEquals("http://new.service.com/context/services/backend",
            sender.getBackendProperties().getWebServiceUrl());
    }

    @Test
    public void testGetMessageStatus() throws Exception {
        getOrCreateConfiguration(backendProperties);
        sender.start();
        final String uuid = Eucegs.uuid();
        when(backendInterface.getStatus(any())).thenReturn(MessageStatus.RECEIVED);

        final MessageStatus messageStatus = sender.getMessageStatus(uuid);
        assertEquals(MessageStatus.RECEIVED, messageStatus);

        try {
            doThrow(new StatusFault(null, null)).when(backendInterface).getStatus(any());
            sender.getMessageStatus(uuid);
            fail();
        } catch (final EucegException ex) {
        }
        verify(i18nService).createKeyedMessage(eq("app.service.euceg.backend.message-status.failed"), eq(uuid));

    }

    @Test
    public void testGetReponse() throws Exception {

        getOrCreateConfiguration(backendProperties);
        sender.start();
        final String uuid = Eucegs.uuid();

        whenRetrieveMessage(uuid, "payload");

        final Response<String> response = sender.getResponse(uuid);
        assertNotNull(response);
        assertEquals(uuid, response.getConversationId(), "conversation id should be same than uuid");
        assertNotNull(response.getResponses());
        assertEquals("payload", response.getResponses().stream().findFirst().orElseThrow());

        try {
            doThrow(new RetrieveMessageFault("failed", null)).when(backendInterface)
                    .retrieveMessage(any(), any(), any());
            sender.getResponse(uuid);
            fail();
        } catch (final EucegException ex) {
            verify(i18nService).createKeyedMessage(eq("app.service.euceg.backend.downloadmessage.failed"));
        }

    }

    @Test
    public void testGetError() throws Exception {
        getOrCreateConfiguration(backendProperties);
        sender.start();
        final String uuid = Eucegs.uuid();
        when(backendInterface.getMessageErrors(any())).thenReturn(new ErrorResultImplArray());

        final ErrorResultImplArray error = sender.getError(uuid);
        assertNotNull(error);

    }

    @Test
    public void testSendPayload() throws Exception {
        getOrCreateConfiguration(backendProperties);
        sender.start();
        final String uuid = Eucegs.uuid();
        final String payload = "payload";
        whenRetrieveMessage(uuid, payload);

        when(backendInterface.submitMessage(any(), any()))
                .thenReturn(new eu.domibus.backend.ws.SubmitResponse().withMessageID(uuid));

        sender.send(uuid, payload, Paths.get("target"));
        final List<EventBackendReceived<IBackendMessage>> events = eventPublisher.getPublishedEvents();
        final SubmitResponse response = (SubmitResponse) events.stream()
                .findFirst()
                .map(EventBackendReceived::getMessage)
                .orElse(null);
        assertNotNull(response);
        assertEquals(uuid, response.getMessageId(), "conversation id should be same than uuid");
        assertEquals(false, response.isErrorMessage());
    }

    private BackendProperties getOrCreateConfiguration(BackendProperties backendProperties) {
        if (backendProperties == null) {
            backendProperties = new BackendProperties();
        }
        backendProperties.setEnable(true);
        backendProperties.setConnectionType(ConnectionType.Ws);
        backendProperties.setFinalRecipient("finalRecipient");
        backendProperties.setFromPartyId("fromPartyId");
        backendProperties.setKeyPairAlias("keyPairAlias");
        backendProperties.setOriginalSender("originalSender");
        backendProperties.setPassword("password");
        backendProperties.setToPartyId("toPartyId");
        backendProperties.setPartyIdType("urn:oasis:names:tc:ebcore:partyid-type:unregistered:EUCEG");
        backendProperties.setTrustedCertificateAlias("trustedCertificateAlias");
        backendProperties.setUrl("http://service.com/");
        backendProperties.setUsername("username");
        return backendProperties;
    }

    @SuppressWarnings("unchecked")
    private void whenRetrieveMessage(final String uuid, final String payload) throws RetrieveMessageFault {
        final Answer<Void> answer = invocation -> {
            final Holder<RetrieveMessageResponse> downloadMessageResponse = (Holder<RetrieveMessageResponse>) invocation
                    .getArguments()[1];
            downloadMessageResponse.value = new RetrieveMessageResponse()
                    .withPayload(new LargePayloadType().withContentType("plain/text")
                            .withValue(new DataHandler(new ByteArrayDataSource(payload, "plain/text"))));
            final Holder<Messaging> ebMSHeaderInfo = (Holder<Messaging>) invocation.getArguments()[2];
            ebMSHeaderInfo.value = new Messaging().withUserMessage(
                new UserMessage().withCollaborationInfo(new CollaborationInfo().withConversationId(uuid)));
            return null;
        };
        doAnswer(answer).when(backendInterface).retrieveMessage(any(), any(), any());
    }

    private BackendProperties getNewConfiguration() {
        final BackendProperties backendProperties = getOrCreateConfiguration(null);
        backendProperties.setUrl("http://new.service.com/context");
        return backendProperties;
    }

    public interface BackendInterfaceClient extends BackendInterface, Client {

    }

}
