package com.pmi.tpd.metrics.gauge;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.JMException;
import javax.management.MBeanServer;
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
public class OperatingSystemGaugeSet implements MetricSet {

    /** */
    private static final String OBJECT_NAME_OPERATING_SYSTEM = "OperatingSystem";

    /** */
    private static final String OBJECT_NAME_RUNTIME_SYSTEM = "Runtime";

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(OperatingSystemGaugeSet.class);

    /** */
    private MBeanServer mBeanServer;

    @Override
    public Map<String, Metric> getMetrics() {
        if (mBeanServer == null) {
            mBeanServer = ManagementFactory.getPlatformMBeanServer();
        }
        final Map<String, Metric> gauges = new HashMap<>();
        try {
            gauges.put("process.cpu.load",
                createJmxAttributeGauge(mBeanServer, OBJECT_NAME_OPERATING_SYSTEM, "ProcessCpuLoad"));
            gauges.put("system.cpu.load",
                createJmxAttributeGauge(mBeanServer, OBJECT_NAME_OPERATING_SYSTEM, "SystemCpuLoad"));
            gauges.put("runtime.java.version",
                createJmxAttributeGauge(mBeanServer, OBJECT_NAME_RUNTIME_SYSTEM, "SpecVersion"));
            gauges.put("runtime.java.vmversion",
                createJmxAttributeGauge(mBeanServer, OBJECT_NAME_RUNTIME_SYSTEM, "VmVersion"));
            gauges.put("system.info", new OperatingSystemInfo());
        } catch (final JMException ex) {
            LOGGER.debug("Unable to load buffer pool MBeans, possibly running on Java 6");
        }
        return gauges;
    }

    private JmxAttributeGauge createJmxAttributeGauge(final MBeanServer mBeanServer,
        final String objectName,
        final String attribute) throws MalformedObjectNameException, IntrospectionException, InstanceNotFoundException,
            ReflectionException {
        final ObjectName on = new ObjectName("java.lang:type=" + objectName);
        mBeanServer.getMBeanInfo(on);
        return new JmxAttributeGauge(mBeanServer, on, attribute);

    }
}