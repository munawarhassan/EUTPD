package com.hazelcast.instance;

import com.hazelcast.config.SocketInterceptorConfig;
import com.hazelcast.core.ManagedContext;
import com.hazelcast.instance.impl.DefaultNodeExtension;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.internal.nio.ClassLoaderUtil;
import com.hazelcast.nio.MemberSocketInterceptor;

/**
 * A subclass of {@link DefaultNodeExtension} which re-enables member socket interceptor support.
 * <p>
 * Unfortunately, this needs to be in the com.hazelcast.instance package so that it can get a reference to the
 * {@link ManagedContext} for the {@link com.hazelcast.core.HazelcastInstance}
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class ExtendedNodeExtension extends DefaultNodeExtension {

    public ExtendedNodeExtension(final Node node) {
        super(node);
    }

    private MemberSocketInterceptor memberSocketInterceptor = null;

    @Override
    public void beforeStart() {
        super.beforeStart();

        final SocketInterceptorConfig config = node.getConfig().getNetworkConfig().getSocketInterceptorConfig();
        if (config != null && config.isEnabled()) {
            memberSocketInterceptor = createInterceptor(config,
                node.getConfigClassLoader(),
                node.getConfig().getManagedContext());
        }

    }

    @Override
    public MemberSocketInterceptor getSocketInterceptor(final EndpointQualifier endpointQualifier) {
        return memberSocketInterceptor;
    }

    private MemberSocketInterceptor createInterceptor(final SocketInterceptorConfig config,
        final ClassLoader classLoader,
        final ManagedContext managedContext) {
        MemberSocketInterceptor interceptor = (MemberSocketInterceptor) config.getImplementation();

        if (interceptor != null) {
            return interceptor;
        }

        try {
            interceptor = ClassLoaderUtil.newInstance(classLoader, config.getClassName());
            interceptor.init(config.getProperties());
            if (managedContext != null) {
                interceptor = (MemberSocketInterceptor) managedContext.initialize(interceptor);
            }
        } catch (final Exception e) {
            logger.warning("Failed to instantiate MemberSocketInterceptor", e);
        }

        return interceptor;
    }
}
