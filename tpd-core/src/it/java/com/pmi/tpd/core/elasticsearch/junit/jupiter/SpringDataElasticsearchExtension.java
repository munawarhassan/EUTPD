package com.pmi.tpd.core.elasticsearch.junit.jupiter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;
import org.testcontainers.DockerClientFactory;

/**
 * This extension class check in the {@link #beforeAll(ExtensionContext)} call if there is already a Elasticsearch
 * cluster connection defined in the root store. If no, the connection to the cluster is defined according to the
 * configuration, starting a local node if necessary. The connection is stored and will be closed when the store is
 * shutdown at the end of all tests.
 * <p/>
 * A ParameterResolver is implemented which enables resolving the ClusterConnectionInfo to test methods, and if a Spring
 * context is used, the ClusterConnectionInfo can be autowired.
 */
public class SpringDataElasticsearchExtension
        implements BeforeAllCallback, ParameterResolver, ContextCustomizerFactory, ExecutionCondition {

    public static final String SPRING_DATA_ELASTICSEARCH_TEST_CLUSTER_URL = "ES_CLUSTER_URL";

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringDataElasticsearchExtension.class);

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
            .create(SpringDataElasticsearchExtension.class.getName());

    private static final String STORE_KEY_CLUSTER_CONNECTION = ClusterConnection.class.getSimpleName();

    private static final String STORE_KEY_CLUSTER_CONNECTION_INFO = ClusterConnectionInfo.class.getSimpleName();

    private static final Lock initLock = new ReentrantLock();

    @Override
    public void beforeAll(final ExtensionContext extensionContext) {
        initLock.lock();
        try {
            final ExtensionContext.Store store = getStore(extensionContext);
            final ClusterConnection clusterConnection = store.getOrComputeIfAbsent(STORE_KEY_CLUSTER_CONNECTION,
                key -> {
                    LOGGER.debug("creating ClusterConnection");
                    return createClusterConnection();
                },
                ClusterConnection.class);
            store.getOrComputeIfAbsent(STORE_KEY_CLUSTER_CONNECTION_INFO,
                key -> clusterConnection.getClusterConnectionInfo());
        } finally {
            initLock.unlock();
        }
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
        return findTestcontainers(context).map(this::evaluate)
                .orElse(findSpringTestcontainers(context).map(this::evaluate)
                        .orElseThrow(() -> new ExtensionConfigurationException(
                                "@Testcontainers or @SpringTestContainers not found")));
    }

    private ExtensionContext.Store getStore(final ExtensionContext extensionContext) {
        return extensionContext.getRoot().getStore(NAMESPACE);
    }

    private ClusterConnection createClusterConnection() {
        return new ClusterConnection(System.getenv(SPRING_DATA_ELASTICSEARCH_TEST_CLUSTER_URL));
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext)
            throws ParameterResolutionException {
        final Class<?> parameterType = parameterContext.getParameter().getType();
        return parameterType.isAssignableFrom(ClusterConnectionInfo.class);
    }

    /*
     * (non javadoc) no need to check the paramaterContext and extensionContext here, this was done before in
     * supportsParameter.
     */
    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return getStore(extensionContext).get(STORE_KEY_CLUSTER_CONNECTION_INFO, ClusterConnectionInfo.class);
    }

    @Override
    public ContextCustomizer createContextCustomizer(final Class<?> testClass,
        final List<ContextConfigurationAttributes> configAttributes) {
        return this::customizeContext;
    }

    private void customizeContext(final ConfigurableApplicationContext context,
        final MergedContextConfiguration mergedConfig) {

        final ClusterConnectionInfo clusterConnectionInfo = ClusterConnection.clusterConnectionInfo();

        if (clusterConnectionInfo != null) {
            context.getBeanFactory().registerResolvableDependency(ClusterConnectionInfo.class, clusterConnectionInfo);
        }
    }

    boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (final Throwable ex) {
            return false;
        }
    }

    private Optional<Testcontainers> findTestcontainers(final ExtensionContext context) {
        Optional<ExtensionContext> current = Optional.of(context);
        while (current.isPresent() && current.get().getTestClass().isPresent()) {
            final Optional<Testcontainers> testcontainers = AnnotationSupport
                    .findAnnotation(current.get().getRequiredTestClass(), Testcontainers.class);
            if (testcontainers.isPresent()) {
                return testcontainers;
            }
            current = current.get().getParent();
        }
        return Optional.empty();
    }

    private Optional<SpringTestcontainers> findSpringTestcontainers(final ExtensionContext context) {
        Optional<ExtensionContext> current = Optional.of(context);
        while (current.isPresent() && current.get().getTestClass().isPresent()) {
            final Optional<SpringTestcontainers> testcontainers = AnnotationSupport
                    .findAnnotation(current.get().getRequiredTestClass(), SpringTestcontainers.class);
            if (testcontainers.isPresent()) {
                return testcontainers;
            }
            current = current.get().getParent();
        }
        return Optional.empty();
    }

    private ConditionEvaluationResult evaluate(final Testcontainers testcontainers) {
        if (testcontainers.disabledWithoutDocker()) {
            if (isDockerAvailable()) {
                return ConditionEvaluationResult.enabled("Docker is available");
            }
            return ConditionEvaluationResult.disabled("disabledWithoutDocker is true and Docker is not available");
        }
        return ConditionEvaluationResult.enabled("disabledWithoutDocker is false");
    }

    private ConditionEvaluationResult evaluate(final SpringTestcontainers testcontainers) {
        if (testcontainers.disabledWithoutDocker()) {
            if (isDockerAvailable()) {
                return ConditionEvaluationResult.enabled("Docker is available");
            }
            return ConditionEvaluationResult.disabled("disabledWithoutDocker is true and Docker is not available");
        }
        return ConditionEvaluationResult.enabled("disabledWithoutDocker is false");
    }

}
