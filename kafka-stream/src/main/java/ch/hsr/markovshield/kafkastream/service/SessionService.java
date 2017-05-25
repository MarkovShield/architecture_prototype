package ch.hsr.markovshield.kafkastream.service;

import ch.hsr.markovshield.models.Session;
import java.util.List;

public interface SessionService {

    List<Session> getAllSessions();

    Session getSession(String sessionId);

    List<Session> getSessionByUser(String user);
}
