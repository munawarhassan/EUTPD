package com.pmi.tpd.service.testing.cluster;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.hazelcast.cluster.Member;
import com.hazelcast.config.Config;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.test.TestHazelcastInstanceFactory;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class HazelcastCluster implements AfterEachCallback, BeforeEachCallback {

    public static String PROP_INDEX = "node-index";

    private final INodeConfigurer configurer;

    private final List<HazelcastInstance> nodes;

    private final int initialSize;

    private final int totalSize;

    private TestHazelcastInstanceFactory factory;

    protected HazelcastCluster(
            final AbstractBuilder<? extends AbstractBuilder<?, ?>, ? extends HazelcastCluster> builder) {
        this.configurer = builder.configurer;
        this.initialSize = builder.initialSize;
        this.totalSize = builder.maxSize;

        this.nodes = Lists.newArrayListWithCapacity(this.totalSize);
    }

    public static int getNodeIndex(final HazelcastInstance node) {
        final String indexProperty = node.getConfig().getProperty(PROP_INDEX);
        if (indexProperty == null) {
            return -1;
        }

        try {
            return Integer.parseInt(indexProperty);
        } catch (final NumberFormatException e) {
        }
        return -1;
    }

    public String generateKeyOwnedByNode(final int index) {
        final HazelcastInstance node = getNode(index);
        while (true) {
            final String key = UUID.randomUUID().toString();
            final Member keyOwner = node.getPartitionService().getPartition(key).getOwner();
            if (keyOwner != null && keyOwner.equals(node.getCluster().getLocalMember())) {
                return key;
            }
        }
    }

    public HazelcastInstance getNode(final int index) {
        return this.nodes.get(index);
    }

    public List<HazelcastInstance> getNodes() {
        return ImmutableList.copyOf(this.nodes);
    }

    public void reset() {
        for (final HazelcastInstance node : this.nodes) {
            resetNode(node);
        }
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        this.factory = new TestHazelcastInstanceFactory(this.totalSize);

        for (int i = 0; i < this.initialSize; ++i) {
            addNode();
        }
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        for (final HazelcastInstance node : this.nodes) {
            tearDownNode(node);
        }
        this.factory.shutdownAll();
        this.factory = null;

        this.nodes.clear();

    }

    public synchronized HazelcastInstance addNode() {
        Preconditions.checkState(this.nodes.size() < this.totalSize,
            "All " + this.totalSize + " nodes have already been added.");

        final Config config = createConfig(this.nodes.size());
        final HazelcastInstance instance = this.factory.newHazelcastInstance(config);
        setupNode(instance);
        this.nodes.add(instance);

        return instance;
    }

    protected Config createConfig(final int nodeIndex) {
        return this.configurer.createConfig(this.nodes.size()).setProperty(PROP_INDEX, Integer.toString(nodeIndex));
    }

    protected void resetNode(final HazelcastInstance node) {
        this.configurer.onReset(node);
        for (final DistributedObject object : node.getDistributedObjects()) {
            object.destroy();
        }
    }

    protected void setupNode(final HazelcastInstance node) {
        this.configurer.postCreate(node);
    }

    protected void tearDownNode(final HazelcastInstance node) {
        this.configurer.preDestroy(node);
    }

    static {
        System.setProperty("hazelcast.logging.type", "slf4j");
        System.setProperty("hazelcast.phone.home.enabled", "false");
    }

    protected static abstract class AbstractBuilder<B extends AbstractBuilder<B, C>, C extends HazelcastCluster> {

        protected INodeConfigurer configurer;

        protected int initialSize;

        protected int maxSize;

        protected AbstractBuilder() {
            this.configurer = new DefaultNodeConfigurer();
            this.initialSize = 1;
            this.maxSize = 1;
        }

        public abstract C build();

        public B nodeConfigurer(final INodeConfigurer value) {
            this.configurer = Preconditions.checkNotNull(value, "value");
            return self();
        }

        public B size(final int value) {
            return size(value, value);
        }

        public B size(final int initial, final int max) {
            Preconditions.checkArgument(initial >= 0, "initial must be positive");
            Preconditions.checkArgument(max >= 0, "max must be positive");
            Preconditions.checkArgument(max >= initial, "max must be equal to or greater than initial");
            this.initialSize = initial;
            this.maxSize = max;
            return self();
        }

        public B standalone() {
            return nodeConfigurer(new StandaloneNodeConfigurer());
        }

        protected abstract B self();
    }

    public static class Builder extends HazelcastCluster.AbstractBuilder<Builder, HazelcastCluster> {

        @Override
        public HazelcastCluster build() {
            return new HazelcastCluster(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
