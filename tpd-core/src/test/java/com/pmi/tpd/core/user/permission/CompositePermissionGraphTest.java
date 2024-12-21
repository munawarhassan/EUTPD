package com.pmi.tpd.core.user.permission;

import static org.mockito.ArgumentMatchers.isNull;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.pmi.tpd.security.permission.IPermissionGraph;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class CompositePermissionGraphTest extends MockitoTestCase {

    @Mock
    private IPermissionGraph graph1;

    @Mock(lenient = true)
    private IPermissionGraph graph2;

    @Test
    public void testMaybeComposeNull() {
        assertEquals(graph1, CompositePermissionGraph.maybeCompose(graph1, null));
        assertEquals(graph1, CompositePermissionGraph.maybeCompose(null, graph1));

        final IPermissionGraph graph = CompositePermissionGraph.maybeCompose(null, null);
        assertNotNull(graph);

        // the returned graph should always return false - even for illegal inputs
        assertFalse(graph.isGranted(null, null));
    }

    @Test
    public void testMaybeCompose() {
        final IPermissionGraph composed = CompositePermissionGraph.maybeCompose(graph1, graph2);
        when(graph1.isGranted(eq(Permission.ADMIN), isNull())).thenReturn(true);
        when(graph2.isGranted(eq(Permission.USER), isNull())).thenReturn(true);

        assertTrue(composed.isGranted(Permission.ADMIN, null));
        assertFalse(composed.isGranted(Permission.SYS_ADMIN, null));
    }

}
