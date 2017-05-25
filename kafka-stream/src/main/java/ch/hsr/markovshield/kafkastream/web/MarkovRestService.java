package ch.hsr.markovshield.kafkastream.web;

import ch.hsr.markovshield.kafkastream.models.HostStoreInfo;
import ch.hsr.markovshield.kafkastream.service.MetadataService;
import ch.hsr.markovshield.kafkastream.service.SessionService;
import ch.hsr.markovshield.kafkastream.service.UserModelService;
import ch.hsr.markovshield.kafkastream.service.ValidatedClickstreamService;
import ch.hsr.markovshield.models.Session;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.models.ValidatedClickStream;
import org.apache.kafka.streams.state.HostInfo;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
     * Changed by Matthias Gabriel 2017
     */

    private final MetadataService metadataService;
    private final SessionService sessionService;
    private final ValidatedClickstreamService validatedClickstreamService;
    private final UserModelService userModelService;
    private final HostInfo hostInfo;
    private Server jettyServer;

    public MarkovRestService(HostInfo hostInfo,
                             MetadataService metadataService,
                             SessionService sessionService,
                             ValidatedClickstreamService validatedClickstreamService,
                             UserModelService userModelService) {
        this.metadataService = metadataService;
        this.sessionService = sessionService;
        this.validatedClickstreamService = validatedClickstreamService;
        this.userModelService = userModelService;
        this.hostInfo = hostInfo;

    }

    @GET
    @Path ("/users/{user}/sessions")
    @Produces (MediaType.APPLICATION_JSON)
    public List<Session> getSessionByUser(@PathParam ("user") final String user) {
        return sessionService.getSessionByUser(user);
    }

    @GET
    @Path ("/users/{user}/validatedclickstreams")
    @Produces (MediaType.APPLICATION_JSON)
    public List<ValidatedClickStream> getValidatedClickstreamsByUser(@PathParam ("user") final String user) {
        return validatedClickstreamService.getValidatedClickstreamsByUser(user);

    }

    @GET
    @Path ("/validatedclickstreams/{sessionUUID}")
    @Produces (MediaType.APPLICATION_JSON)
    public ValidatedClickStream getValidatedClickstreamByUUID(@PathParam ("sessionUUID") final String uuid) {
        return validatedClickstreamService.getValidatedClickstream(uuid);
    }

    @GET
    @Path ("/local/validatedclickstreams")
    @Produces (MediaType.APPLICATION_JSON)
    public List<ValidatedClickStream> getLocalValidatedClickstreams() {
        return validatedClickstreamService.getLocalValidatedClickstreams();
    }

    @GET
    @Path ("/validatedclickstreams")
    @Produces (MediaType.APPLICATION_JSON)
    public List<ValidatedClickStream> getAllValidatedClickstreams() {
        return validatedClickstreamService.getAllValidatedClickstreams();
    }

    @GET
    @Path ("/user/{user}/usermodel")
    @Produces (MediaType.APPLICATION_JSON)
    public UserModel getUserModelByUser(@PathParam ("user") final String user) {
        return userModelService.getUserModel(user);
    }


    @GET
    @Path ("/usermodels")
    @Produces (MediaType.APPLICATION_JSON)
    public List<UserModel> getAllUserModels() {
        return userModelService.getAllUserModels();
    }

    @GET
    @Path ("/sessions/{sessionId}")
    @Produces (MediaType.APPLICATION_JSON)
    public Session getSession(@PathParam ("sessionId") final String sessionId) {

        return sessionService.getSession(sessionId);

    }

    @GET
    @Path ("/sessions")
    @Produces (MediaType.APPLICATION_JSON)
    public List<Session> getAllSession() {
        return sessionService.getAllSessions();
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
    public void start() throws Exception {
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
    public void stop() throws Exception {
        if (jettyServer != null) {
            jettyServer.stop();
        }
    }

}
