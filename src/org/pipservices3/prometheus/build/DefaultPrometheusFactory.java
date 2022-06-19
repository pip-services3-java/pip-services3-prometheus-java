package org.pipservices3.prometheus.build;

import org.pipservices3.commons.refer.Descriptor;
import org.pipservices3.components.build.Factory;
import org.pipservices3.prometheus.count.PrometheusCounters;
import org.pipservices3.prometheus.services.PrometheusMetricsService;

/**
 * Creates Prometheus components by their descriptors.
 *
 * @see Factory
 * @see org.pipservices3.prometheus.count.PrometheusCounters
 * @see org.pipservices3.prometheus.services.PrometheusMetricsService
 */
public class DefaultPrometheusFactory extends Factory {
    private static final Descriptor PrometheusCountersDescriptor = new Descriptor("pip-services", "counters", "prometheus", "*", "1.0");
    private static final Descriptor PrometheusMetricsServiceDescriptor = new Descriptor("pip-services", "metrics-service", "prometheus", "*", "1.0");

    /**
     * Create a new instance of the factory.
     */
    public DefaultPrometheusFactory() {
        super();
        this.registerAsType(DefaultPrometheusFactory.PrometheusCountersDescriptor, PrometheusCounters.class);
        this.registerAsType(DefaultPrometheusFactory.PrometheusMetricsServiceDescriptor, PrometheusMetricsService.class);
    }
}
