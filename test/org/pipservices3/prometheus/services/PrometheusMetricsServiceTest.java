package org.pipservices3.prometheus.services;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.*;
import org.pipservices3.commons.config.ConfigParams;
import org.pipservices3.commons.errors.ApplicationException;
import org.pipservices3.commons.refer.Descriptor;
import org.pipservices3.commons.refer.References;
import org.pipservices3.components.count.CounterType;
import org.pipservices3.components.info.ContextInfo;
import org.pipservices3.prometheus.count.PrometheusCounters;
import org.pipservices3.prometheus.count.PrometheusCountersTest;

import static org.junit.Assert.*;

public class PrometheusMetricsServiceTest {
    static final ConfigParams restConfig = ConfigParams.fromTuples(
            "connection.protocol", "http",
            "connection.host", "localhost",
            "connection.port", 3000
    );
    static PrometheusMetricsService service;
    static PrometheusCounters counters;
    Client rest;

    @BeforeClass
    public static void setupClass() throws ApplicationException {
        service = new PrometheusMetricsService();
        service.configure(restConfig);

        counters = new PrometheusCounters();

        var contextInfo = new ContextInfo();
        contextInfo.setName("Test");
        contextInfo.setDescription("This is a test container");

        var references = References.fromTuples(
                new Descriptor("pip-services", "context-info", "default", "default", "1.0"), contextInfo,
                new Descriptor("pip-services", "counters", "prometheus", "default", "1.0"), counters,
                new Descriptor("pip-services", "metrics-service", "prometheus", "default", "1.0"), service
        );
        counters.setReferences(references);
        service.setReferences(references);

        counters.open(null);
        service.open(null);
    }

    @AfterClass
    public static void teardownClass() throws ApplicationException {
        service.close(null);
        counters.close(null);
    }

    @Before
    public void setup() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(new JacksonFeature());
        rest = ClientBuilder.newClient(clientConfig);
    }

    @After
    public void teardown() {
        rest.close();
        rest = null;
    }

    @Test
    public void testMetrics() {
        counters.incrementOne("test.counter1");
        counters.stats("test.counter2", 2);
        counters.last("test.counter3", 3);
        counters.timestampNow("test.counter4");

        var response = invoke("/metrics", null);
        assertNotNull(response);
        assertTrue(response.getStatus() < 400);
        assertTrue(response.getLength() > 0);
    }

    @Test
    public void testMetricsAndReset() {
        counters.incrementOne("test.counter1");
        counters.stats("test.counter2", 2);
        counters.last("test.counter3", 3);
        counters.timestampNow("test.counter4");

        var response = invoke("/metricsandreset", null);

        assertNotNull(response);
        assertTrue(response.getStatus() < 400);
        assertTrue(response.getLength() > 0);

        var counter1 = counters.get("test.counter1", CounterType.Increment);
        var counter2 = counters.get("test.counter2", CounterType.Statistics);
        var counter3 = counters.get("test.counter3", CounterType.LastValue);
        var counter4 = counters.get("test.counter4", CounterType.Timestamp);

        assertNull(counter1.getCount());
        assertNull(counter2.getCount());
        assertNull(counter3.getLast());
        assertNull(counter4.getTime());
    }

    private Response invoke(String route, Object entity) {
        try (Response response = rest.target("http://localhost:3000" + route)
                .request(MediaType.TEXT_PLAIN)
                .method(HttpMethod.GET, Entity.entity(entity, MediaType.TEXT_PLAIN));) {

            return response;
        }
    }
}
