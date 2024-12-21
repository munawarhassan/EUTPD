package com.pmi.tpd.service.testing.cluster;

import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ManagedContext;

public class SpringManagedCluster extends HazelcastCluster {

    private final Map<Integer, List<Object>> beans = Maps.newHashMap();

    protected SpringManagedCluster(final Builder builder) {
        super(builder);

        for (final Map.Entry<Integer, List<Object>> entry : builder.beans.entrySet()) {
            this.beans.put(entry.getKey(), Lists.newArrayList(entry.getValue()));
        }
    }

    public void registerBeans(final Object... beans) {
        for (final HazelcastInstance node : getNodes()) {
            getManagedContext(node).addBeans(Lists.newArrayList(beans));
        }
    }

    public void registerBeansForNode(final int nodeIndex, final Object... beans) {
        getManagedContext(getNode(nodeIndex)).addBeans(Lists.newArrayList(beans));
    }

    @Override
    protected Config createConfig(final int nodeIndex) {
        return super.createConfig(nodeIndex).setManagedContext(new ResettableSpringManagedContext());
    }

    @Override
    protected void resetNode(final HazelcastInstance node) {
        super.resetNode(node);
        initManagedContext(node);
    }

    @Override
    protected void setupNode(final HazelcastInstance node) {
        super.setupNode(node);
        initManagedContext(node);
    }

    @Override
    protected void tearDownNode(final HazelcastInstance node) {
        final ResettableSpringManagedContext managedContext = getManagedContext(node);
        if (managedContext != null) {
            managedContext.destroy();
            // node.getConfig().setManagedContext(null);
        }
        super.tearDownNode(node);
    }

    private ResettableSpringManagedContext getManagedContext(final HazelcastInstance node) {
        final ManagedContext context = node.getConfig().getManagedContext();
        return context instanceof ResettableSpringManagedContext ? (ResettableSpringManagedContext) context : null;
    }

    private void initManagedContext(final HazelcastInstance node) {
        final ResettableSpringManagedContext managedContext = getManagedContext(node);
        if (managedContext != null) {
            managedContext.reset();
            managedContext.addBeans(this.beans.get(Integer.valueOf(-999)));
            managedContext.addBeans(this.beans.get(Integer.valueOf(getNodeIndex(node))));
        }
    }

    public static class Builder extends HazelcastCluster.AbstractBuilder<Builder, SpringManagedCluster> {

        private final Map<Integer, List<Object>> beans;

        public Builder() {
            this.beans = Maps.newHashMap();
        }

        public Builder beans(final Object[] values) {
            return beansForNode(-999, values);
        }

        public Builder beansForNode(final int nodeIndex, final Object[] values) {
            List<Object> nodeBeans = this.beans.get(Integer.valueOf(nodeIndex));
            if (nodeBeans == null) {
                nodeBeans = Lists.newArrayList();
                this.beans.put(Integer.valueOf(nodeIndex), nodeBeans);
            }
            for (final Object v : values) {
                nodeBeans.add(Preconditions.checkNotNull(v, "values contains a null value"));
            }
            return self();
        }

        @Override
        public SpringManagedCluster build() {
            return new SpringManagedCluster(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
