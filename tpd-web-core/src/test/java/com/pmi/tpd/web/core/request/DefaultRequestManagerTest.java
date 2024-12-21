package com.pmi.tpd.web.core.request;

import static org.mockito.ArgumentMatchers.isA;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.cluster.IClusterInformation;
import com.pmi.tpd.cluster.IClusterNode;
import com.pmi.tpd.cluster.IClusterService;
import com.pmi.tpd.cluster.event.ClusterNodeAddedEvent;
import com.pmi.tpd.cluster.event.ClusterNodeRemovedEvent;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.testing.junit5.MockitoTestCase;
import com.pmi.tpd.web.core.request.event.RequestEndedEvent;
import com.pmi.tpd.web.core.request.event.RequestStartedEvent;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

/**
 * unit test for {@link DefaultRequestManager}
 */

public class DefaultRequestManagerTest extends MockitoTestCase {

    public static final String PROTOCOL = "protocol";

    public static final String DETAILS = "details";

    public static final String REQUEST = "request";

    public static final String ADDRESS = "address";

    public static final String SESSION_ID = "sessionId";

    public static final UUID LOCAL_NODE_ID = UUID.randomUUID();

    @Mock
    private IAuthenticationContext authContext;

    @Mock
    IClusterInformation clusterInformation;

    @Mock
    IClusterNode clusterNode;

    @Mock(lenient = true)
    IClusterService clusterService;

    @Mock
    private IEventPublisher eventPublisher;

    @Mock
    private IRequestInfoProvider requestInfoProvider;

    private DefaultRequestManager requestManager;

    @BeforeEach
    public void setupRequestInfoProvider() {
        when(clusterNode.getId()).thenReturn(LOCAL_NODE_ID);
        when(clusterInformation.getLocalNode()).thenReturn(clusterNode);
        when(clusterService.getInformation()).thenReturn(clusterInformation);
        when(clusterService.isAvailable()).thenReturn(false);

        when(requestInfoProvider.getProtocol()).thenReturn(PROTOCOL);
        when(requestInfoProvider.getDetails()).thenReturn(DETAILS);
        when(requestInfoProvider.getRawRequest()).thenReturn(REQUEST);
        when(requestInfoProvider.getRemoteAddress()).thenReturn(ADDRESS);
        when(requestInfoProvider.getSessionId()).thenReturn(SESSION_ID);

        createRequestManager();
    }

    @Test
    public void testReturnsCallbackReturnValue() {
        final String returnValue = "return";

        assertEquals(returnValue,
            requestManager.doAsRequest((@Nonnull final IRequestContext requestContext) -> returnValue,
                requestInfoProvider));
    }

    @Test
    public void testSendsRequestEvents() {
        requestManager.doAsRequest((@Nonnull final IRequestContext requestContext) -> {
            verifyRequestContext(requestContext);

            // verify that the RequestStartedEvent has been sent and that it is backed by
            // the data provided by the
            // requestInfoProvider
            final ArgumentCaptor<RequestStartedEvent> startedEvent = ArgumentCaptor.forClass(RequestStartedEvent.class);
            verify(eventPublisher).publish(startedEvent.capture());
            assertSame(requestContext, startedEvent.getValue().getRequestContext());

            verify(eventPublisher, never()).publish(isA(RequestEndedEvent.class));
            reset(eventPublisher);
            return null;
        }, requestInfoProvider);

        final ArgumentCaptor<RequestEndedEvent> endedEvent = ArgumentCaptor.forClass(RequestEndedEvent.class);
        verify(eventPublisher).publish(endedEvent.capture());
        verifyRequestContext(endedEvent.getValue().getRequestContext());

    }

    @Test
    public void testNoNestedRequests() throws Exception {
        requestManager.doAsRequest((@Nonnull final IRequestContext outerContext) -> requestManager
                .doAsRequest((@Nonnull final IRequestContext innerContext) -> {
                    assertSame(outerContext, innerContext);
                    return null;
                }, mock(IRequestInfoProvider.class)),
            requestInfoProvider);
    }

    @Test
    public void testRequestMetaDataInRequest() {
        requestManager.doAsRequest((@Nonnull final IRequestContext outerContext) -> {
            assertNotNull(requestManager.getRequestMetadata());
            assertEquals(requestManager.getRequestMetadata(), requestManager.getRequestContext());
            return null;
        }, requestInfoProvider);
    }

    @Test
    public void testRequestMetaDataAfterStatefulApply() {
        requestManager.doAsRequest((@Nonnull final IRequestContext outerContext) -> {
            requestManager.getState().apply();
            // should now have our own copy of the state
            assertNotNull(requestManager.getRequestMetadata());
            assertNotEquals(requestManager.getRequestMetadata(), requestManager.getRequestContext());
            return null;
        }, requestInfoProvider);
    }

    @Test
    public void testRequestMetaDataAfterStatefulRemove() {
        requestManager.doAsRequest((@Nonnull final IRequestContext outerContext) -> {
            requestManager.getState().remove();
            // should now have reverted to picking up the original provider
            assertNotNull(requestManager.getRequestMetadata());
            assertEquals(requestManager.getRequestMetadata(), requestManager.getRequestContext());

            return null;
        }, requestInfoProvider);
    }

    @Test
    public void testRequestIdWhenClustered() {
        when(clusterService.isClustered()).thenReturn(true);

        createRequestManager();

        assertClusteredRequestId();
    }

    @Test
    public void testRequestIdWhenNotClustered() {
        when(clusterService.isClustered()).thenReturn(false);

        createRequestManager();

        assertNonClusteredRequestId();
    }

    @Test
    public void testRequestIdWhenNotClusteredThenClusteredThenNonClustered() {
        final Set<IClusterNode> nodes = ImmutableSet.<IClusterNode> builder()
                .add(mock(IClusterNode.class))
                .add(mock(IClusterNode.class))
                .build();

        createRequestManager();

        assertNonClusteredRequestId();

        requestManager.onClusterMembershipChanged(new ClusterNodeAddedEvent(this, mock(IClusterNode.class), nodes));

        assertClusteredRequestId();

        requestManager.onClusterMembershipChanged(
            new ClusterNodeRemovedEvent(this, mock(IClusterNode.class), Collections.<IClusterNode> emptySet()));

        assertNonClusteredRequestId();
    }

    private void assertClusteredRequestId() {
        requestManager.doAsRequest((@Nonnull final IRequestContext outerContext) -> {
            assertTrue(Pattern.compile("\\*[A-Z1-9]+x\\d+x\\d+x\\d+").matcher(outerContext.getId()).matches(),
                outerContext.getId() + " doesn't match expected clustered request pattern");
            return null;
        }, requestInfoProvider);
    }

    private void assertNonClusteredRequestId() {
        requestManager.doAsRequest((@Nonnull final IRequestContext outerContext) -> {
            assertTrue(Pattern.compile("@[A-Z1-9]+x\\d+x\\d+x\\d+").matcher(outerContext.getId()).matches(),
                outerContext.getId() + " doesn't match expected non-clustered request pattern");
            return null;
        }, requestInfoProvider);
    }

    private void verifyRequestContext(final IRequestContext context) {
        assertNotNull(context);
        assertNotNull(context.getId());
        assertEquals(PROTOCOL, context.getProtocol());
        assertEquals(DETAILS, context.getDetails());
        assertEquals(REQUEST, context.getRawRequest());
        assertEquals(ADDRESS, context.getRemoteAddress());
        assertEquals(SESSION_ID, context.getSessionId());
    }

    private void createRequestManager() {
        requestManager = new DefaultRequestManager(authContext, eventPublisher, clusterService);
    }
}
