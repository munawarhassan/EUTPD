package com.pmi.tpd.metrics.gauge;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.JmxAttributeGauge;

/**
 * Provides gauges with basic operating system info, system load average and current system CPU utilization.
 *
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
@ParametersAreNonnullByDefault
public class BoneCpGaugeSet implements MetricSet {

    /** */
    private static final String OBJECT_NAME_BONECP = "BoneCP";

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(BoneCpGaugeSet.class);

    /** */
    private MBeanServer mBeanServer;

    @Override
    public Map<String, Metric> getMetrics() {
        if (mBeanServer == null) {
            mBeanServer = getMBeanServer();
        }
        final Map<String, Metric> gauges = new HashMap<>();
        try {
            gauges.put("statement.execute.avg",
                createJmxAttributeGauge(mBeanServer, OBJECT_NAME_BONECP, "StatementExecuteTimeAvg"));
            gauges.put("statement.prepare.avg",
                createJmxAttributeGauge(mBeanServer, OBJECT_NAME_BONECP, "StatementPrepareTimeAvg"));
            gauges.put("statement.prepare.avg",
                createJmxAttributeGauge(mBeanServer, OBJECT_NAME_BONECP, "StatementPrepareTimeAvg"));
            gauges.put("connection.total.created",
                createJmxAttributeGauge(mBeanServer, OBJECT_NAME_BONECP, "TotalCreatedConnections"));
            gauges.put("connection.total.free", createJmxAttributeGauge(mBeanServer, OBJECT_NAME_BONECP, "TotalFree"));
            gauges.put("connection.total.leased",
                createJmxAttributeGauge(mBeanServer, OBJECT_NAME_BONECP, "TotalLeased"));
            gauges.put("connection.wait.avg",
                createJmxAttributeGauge(mBeanServer, OBJECT_NAME_BONECP, "ConnectionWaitTimeAvg"));

        } catch (final JMException ex) {
            LOGGER.debug("Unable to load buffer pool MBeans, possibly running on Java 6");
        }
        return gauges;
    }

    private static MBeanServer getMBeanServer() {
        final List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
        final MBeanServer server = servers.size() > 0 ? servers.get(0) : ManagementFactory.getPlatformMBeanServer();
        return server;
    }

    private JmxAttributeGauge createJmxAttributeGauge(final MBeanServer mBeanServer,
        final String objectName,
        final String attribute) throws MalformedObjectNameException, IntrospectionException, InstanceNotFoundException,
            ReflectionException {
        final ObjectName on = new ObjectName("com.jolbox.bonecp:type=" + objectName);
        return new JmxAttributeGauge(mBeanServer, on, attribute);

    }
}