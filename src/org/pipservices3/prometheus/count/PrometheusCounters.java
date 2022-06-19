package org.pipservices3.prometheus.count;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.pipservices3.commons.config.ConfigParams;
import org.pipservices3.commons.config.IConfigurable;
import org.pipservices3.commons.errors.ApplicationException;
import org.pipservices3.commons.errors.ConfigException;
import org.pipservices3.commons.errors.InvocationException;
import org.pipservices3.commons.refer.Descriptor;
import org.pipservices3.commons.refer.IReferenceable;
import org.pipservices3.commons.refer.IReferences;
import org.pipservices3.commons.refer.ReferenceException;
import org.pipservices3.commons.run.IOpenable;
import org.pipservices3.components.count.CachedCounters;
import org.pipservices3.components.count.Counter;
import org.pipservices3.components.info.ContextInfo;
import org.pipservices3.components.log.CompositeLogger;
import org.pipservices3.rpc.connect.HttpConnectionResolver;


import java.net.InetAddress;
import java.util.List;

public class PrometheusCounters extends CachedCounters implements IReferenceable, IOpenable, IConfigurable {
    private String _baseRoute;
    private final CompositeLogger _logger = new CompositeLogger();
    private final HttpConnectionResolver _connectionResolver = new HttpConnectionResolver();
    private boolean _opened = false;
    private String _source;
    private String _instance;
    private boolean _pushEnabled;
    private Client _client;
    private String _requestRoute;

    /**
     * Configures component by passing configuration parameters.
     *
     * @param config configuration parameters to be set.
     */
    @Override
    public void configure(ConfigParams config) {
        super.configure(config);

        this._connectionResolver.configure(config);
        this._source = config.getAsStringWithDefault("source", this._source);
        this._instance = config.getAsStringWithDefault("instance", this._instance);
        this._pushEnabled = config.getAsBooleanWithDefault("push_enabled", true);
    }

    /**
     * Sets references to dependent components.
     *
     * @param references references to locate the component dependencies.
     */
    @Override
    public void setReferences(IReferences references) {
        this._logger.setReferences(references);
        this._connectionResolver.setReferences(references);

        var contextInfo = references.getOneOptional(ContextInfo.class,
                new Descriptor("pip-services", "context-info", "default", "*", "1.0"));

        if (contextInfo != null && this._source == null)
            this._source = contextInfo.getName();

        if (contextInfo != null && this._instance == null)
            this._instance = contextInfo.getContextId();
    }

    /**
     * Checks if the component is opened.
     *
     * @return true if the component has been opened and false otherwise.
     */
    @Override
    public boolean isOpen() {
        return this._opened;
    }

    /**
     * Opens the component.
     *
     * @param correlationId (optional) transaction id to trace execution through call chain.
     */
    @Override
    public void open(String correlationId) {
        if (this._opened)
            return;

        if (!this._pushEnabled)
            return;

        this._opened = true;

        try {
            var connection = this._connectionResolver.resolve(correlationId);
            _baseRoute = connection.getAsString("uri");

            var job = this._source != null ? _source : "unknown";
            var instance = _instance != null ? _instance : InetAddress.getLocalHost().getHostName();
            this._requestRoute = "/metrics/job/" + job + "/instance/" + instance;

            ClientConfig clientConfig = new ClientConfig();
            clientConfig.register(new JacksonFeature());

            _client = ClientBuilder.newClient(clientConfig);
        } catch (Exception ex) {
            this._client = null;
            this._logger.warn(correlationId, "Connection to Prometheus server is not configured: " + ex);
        }
    }

    /**
     * Closes component and frees used resources.
     *
     * @param correlationId (optional) transaction id to trace execution through call chain.
     */
    @Override
    public void close(String correlationId) {
        if (_client != null)
            _client.close();

        this._opened = false;
        this._client = null;
        this._requestRoute = null;
    }

    /**
     * Saves the current counters measurements.
     *
     * @param counters current counters measurements to be saves.
     */
    @Override
    protected void save(List<Counter> counters) {
        if (this._client == null || !this._pushEnabled) return;

        String body = PrometheusCounterConverter.toString(counters, null, null);

        try (Response response = _client.target(_baseRoute + _requestRoute).request(MediaType.APPLICATION_JSON).put(Entity.entity(body, MediaType.APPLICATION_JSON))) {
            if (response.getStatus() >= 400)
                this._logger.error("prometheus-counters", response.readEntity(String.class), "Failed to push metrics to prometheus");
        }
    }
}
