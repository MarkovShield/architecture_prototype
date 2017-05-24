package ch.hsr.markovshield.kafkastream.service;

import ch.hsr.markovshield.models.Session;
import java.util.List;

/**
 * Created by maede on 24.05.2017.
 */
public interface SessionService {

    List<Session> getAllSessions();

    Session getSession(String sessionId);
}
