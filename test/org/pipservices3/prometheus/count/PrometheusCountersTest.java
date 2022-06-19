package org.pipservices3.prometheus.count;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pipservices3.commons.config.ConfigParams;
import org.pipservices3.commons.errors.ApplicationException;
import org.pipservices3.commons.errors.InvocationException;
import org.pipservices3.prometheus.fixtures.CountersFixture;

public class PrometheusCountersTest {
    PrometheusCounters _counters;
    CountersFixture _fixture;

    @Before
    public void setup() throws ApplicationException {
        var host = System.getenv("PUSHGATEWAY_SERVICE_HOST") != null ? System.getenv("PUSHGATEWAY_SERVICE_HOST") : "localhost";
        var port = System.getenv("PUSHGATEWAY_SERVICE_PORT") != null ? Integer.parseInt(System.getenv("PUSHGATEWAY_SERVICE_PORT")) : 9091;

        _counters = new PrometheusCounters();
        _fixture = new CountersFixture(_counters);

        var config = ConfigParams.fromTuples(
                "source", "test",
                "connection.host", host,
                "connection.port", port
        );
        _counters.configure(config);

        _counters.open(null);
    }

    @After
    public void teardown() throws ApplicationException {
        _counters.close(null);
    }

    @Test
    public void testSimpleCounters() throws InvocationException, InterruptedException {
        _fixture.testSimpleCounters();
    }

    @Test
    public void testMeasureElapsedTime() throws InvocationException, InterruptedException {
        _fixture.testMeasureElapsedTime();
    }
}
