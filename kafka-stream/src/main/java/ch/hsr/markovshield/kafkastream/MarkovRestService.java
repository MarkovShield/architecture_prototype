package ch.hsr.markovshield.kafkastream;

/**
 * Created by maede on 17.05.2017.
 */

import ch.hsr.markovshield.models.Session;
import ch.hsr.markovshield.models.UserModel;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path ("markovShield")
public class MarkovRestService {

    /**
     * Copyright 2016 Confluent Inc.
     *
     * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
     * in compliance with the License. You may obtain a copy of the License at
     *
     * http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software distributed under the License
     * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
     * or implied. See the License for the specific language governing permissions and limitations under
     * the License.
     */


    /**
     * A simple REST proxy that runs embedded in the  This is used to
     * demonstrate how a developer can use the Interactive Queries APIs exposed by Kafka Streams to
     * locate and query the State Stores within a Kafka Streams Application.
     */

    private final KafkaStreams streams;
    private final MetadataService metadataService;
    private final HostInfo hostInfo;
    private final Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
    private Server jettyServer;
    private LongSerializer serializer = new LongSerializer();


    MarkovRestService(final KafkaStreams streams, final HostInfo hostInfo) {
        this.streams = streams;
        this.metadataService = new MetadataService(streams);
        this.hostInfo = hostInfo;
    }


    @GET
    @Path ("/usermodels/{user}")
    @Produces (MediaType.APPLICATION_JSON)
    public UserModel getUserModelByUser(@PathParam ("user") final String user) {

        // The genre might be hosted on another instance. We need to find which instance it is on
        // and then perform a remote lookup if necessary.
        return getValueFromAnyStore(user, MarkovClickStreamProcessing.MARKOV_USER_MODEL_STORE, "usermodels/" + user);

    }

    private <T> T getValueFromAnyStore(String key, String markovLoginStore, String path) {
        final HostStoreInfo
            host =
            metadataService.streamsMetadataForStoreAndKey(markovLoginStore, key, new
                StringSerializer());

        // genre is on another instance. call the other instance to fetch the data.
        if (!thisHost(host)) {
            return fetchValueFromOtherHost(host, path);
        }

        // genre is on this instance
        return getValueFromStore(key, markovLoginStore);
    }

    private boolean thisHost(final HostStoreInfo host) {
        return host.getHost().equals(hostInfo.host()) &&
            host.getPort() == hostInfo.port();
    }

    private <T> T fetchValueFromOtherHost(final HostStoreInfo host, final String path) {
        return client.target(String.format("http://%s:%d/%s", host.getHost(), host.getPort(), path))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(new GenericType<T>() {
            });
    }

    private <T> T getValueFromStore(final String key,
                                    final String storeName) {

        final ReadOnlyKeyValueStore<String, T> userModels =
            streams.store(storeName, QueryableStoreTypes.<String, T>keyValueStore());
        // Get the value from the store
        final T value = userModels.get(key);
        System.out.println(key);
        System.out.println(value);
        if (value == null) {
            throw new NotFoundException(String.format("Unable to find value in %s for key %s", storeName, key));
        }
        return value;
    }

    @GET
    @Path ("/usermodels")
    @Produces (MediaType.APPLICATION_JSON)
    public List<UserModel> getAllUserModels() {

        // The genre might be hosted on another instance. We need to find which instance it is on
        // and then perform a remote lookup if necessary.
        List<UserModel> allValuesFromOtherStores = getAllValuesFromAllStores(
            MarkovClickStreamProcessing.MARKOV_USER_MODEL_STORE,
            "/local/usermodels");
        return allValuesFromOtherStores;

    }

    private <T> List<T> getAllValuesFromAllStores(String store, String path) {
        List<T> allValuesFromOtherStores = getAllValuesFromOtherStores(store,
            path);
        allValuesFromOtherStores.addAll(getAllValuesFromLocalStore(store));
        return allValuesFromOtherStores;
    }

    private <T> List<T> getAllValuesFromOtherStores(String markovUserModelStore, String path) {
        List<HostStoreInfo> hostStoreInfos = metadataService.streamsMetadata(markovUserModelStore);
        List<T> allModels = new ArrayList<>();
        for (HostStoreInfo info : hostStoreInfos
            ) {
            if (!thisHost(info)) {
                List<UserModel> list = fetchValueFromOtherHost(info, path);
                allModels.addAll(allModels);
            }
        }
        return allModels;
    }

    private <T> List<T> getAllValuesFromLocalStore(final String storeName) {
        final ReadOnlyKeyValueStore<String, T> userModels =
            streams.store(storeName, QueryableStoreTypes.<String, T>keyValueStore());

        List<T> allValues = new ArrayList<>();
        KeyValueIterator<String, T> all = userModels.all();
        for (KeyValueIterator<String, T> it = all; it.hasNext(); ) {
            KeyValue<String, T> x = it.next();
            allValues.add(x.value);
        }
        return allValues;
    }

    @GET
    @Path ("/local/usermodels")
    @Produces (MediaType.APPLICATION_JSON)
    public List<UserModel> getAllLocalUserModels() {

        // The genre might be hosted on another instance. We need to find which instance it is on
        // and then perform a remote lookup if necessary.
        return getAllValuesFromLocalStore(MarkovClickStreamProcessing.MARKOV_USER_MODEL_STORE);

    }

    @GET
    @Path ("/sessions/{sessionId}")
    @Produces (MediaType.APPLICATION_JSON)
    public Session getSession(@PathParam ("sessionId") final String sessionId) {

        // The genre might be hosted on another instance. We need to find which instance it is on
        // and then perform a remote lookup if necessary.
        return getValueFromAnyStore(sessionId,
            MarkovClickStreamProcessing.MARKOV_LOGIN_STORE,
            "sessions/" + sessionId);

    }

    @GET
    @Path ("/sessions")
    @Produces (MediaType.APPLICATION_JSON)
    public List<Session> getAllSession() {

        // The genre might be hosted on another instance. We need to find which instance it is on
        // and then perform a remote lookup if necessary.

        return getAllValuesFromAllStores(MarkovClickStreamProcessing.MARKOV_LOGIN_STORE, "/local/sessions");
    }

    /**
     * Get the metadata for all of the instances of this Kafka Streams application
     *
     * @return List of {@link HostStoreInfo}
     */
    @GET ()
    @Path ("/instances")
    @Produces (MediaType.APPLICATION_JSON)
    public List<HostStoreInfo> streamsMetadata() {
        return metadataService.streamsMetadata();
    }

    /**
     * Get the metadata for all instances of this Kafka Streams application that currently
     * has the provided store.
     *
     * @param store The store to locate
     * @return List of {@link HostStoreInfo}
     */
    @GET ()
    @Path ("/instances/{storeName}")
    @Produces (MediaType.APPLICATION_JSON)
    public List<HostStoreInfo> streamsMetadataForStore(@PathParam ("storeName") String store) {
        return metadataService.streamsMetadataForStore(store);
    }

    /**
     * Start an embedded Jetty Server
     *
     * @throws Exception
     */
    void start() throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        jettyServer = new Server(hostInfo.port());
        jettyServer.setHandler(context);

        ResourceConfig rc = new ResourceConfig();
        rc.register(this);
        rc.register(JacksonFeature.class);

        ServletContainer sc = new ServletContainer(rc);
        ServletHolder holder = new ServletHolder(sc);
        context.addServlet(holder, "/*");

        jettyServer.start();
    }

    /**
     * Stop the Jetty Server
     *
     * @throws Exception
     */
    void stop() throws Exception {
        if (jettyServer != null) {
            jettyServer.stop();
        }
    }

}
