package com.pmi.tpd.metrics;

/**
 * A service that can be used to submit a named double value for storage and analysis. Any statistics or analysis that
 * needs to be carried out is best left for other concerns, but ultimately they are under control of the implementation
 * of this service. For instance, the value submitted here could be a method execution timing result, and it would go to
 * a backend that keeps a histogram of recent values for comparison purposes. Or it could be a simple measurement of a
 * sensor value (like a temperature reading) to be passed on to a monitoring system in its raw form.
 */
public interface GaugeService {

    /**
     * Set the specified gauge value.
     *
     * @param metricName
     *            the name of the gauge to set
     * @param value
     *            the value of the gauge
     */
    void submit(String metricName, double value);

}
