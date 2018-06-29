package moe.lyrebird.model.sessions;

import org.springframework.context.ApplicationContext;
import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import moe.lyrebird.model.twitter.twitter4j.TwitterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.auth.AccessToken;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The session manager is responsible for persisting the sessions in database and providing handles to them should
 * another component need access to them (i.e. the JavaFX controllers per example).
 */
public class SessionManager {

    private static final Logger LOG = LoggerFactory.getLogger(SessionManager.class);

    private final ApplicationContext context;
    private final SessionRepository sessionRepository;

    private final Set<Session> loadedSessions = new HashSet<>();

    private final Property<Session> currentSession;
    private final Property<String> currentSessionUsername;

    public SessionManager(final ApplicationContext context, final SessionRepository sessionRepository) {
        this.context = context;
        this.sessionRepository = sessionRepository;
        this.currentSession = new SimpleObjectProperty<>(null);
        this.currentSessionUsername = new SimpleStringProperty("Logged out");
        this.currentSession.addListener(
                (ref, oldVal, newVal) -> {
                    LOG.debug("Current session property changed from {} to {}", oldVal, newVal);
                    currentSessionUsername.setValue(newVal.getUserScreenName());
                }
        );
    }

    public Property<Session> currentSessionProperty() {
        return currentSession;
    }

    public boolean isLoggedIn() {
        return currentSession.getValue() != null;
    }

    public Property<String> currentSessionUsernameProperty() {
        return currentSessionUsername;
    }

    public Optional<Session> getSessionForUser(final String userId) {
        return this.loadedSessions.stream()
                                  .filter(session -> session.getUserId().equals(userId))
                                  .findFirst();
    }

    public Try<Twitter> getCurrentTwitter() {
        return Try.of(() -> currentSession)
                  .map(Property::getValue)
                  .map(Session::getTwitterHandler)
                  .map(TwitterHandler::getTwitter)
                  .andThenTry(session -> LOG.debug(
                          "Preparing request for user : {}",
                          session.getScreenName()
                  ));
    }

    public <T> Try<T> doWithCurrentTwitter(final CheckedFunction1<Twitter, T> action) {
        return getCurrentTwitter().mapTry(action);
    }

    /**
     * Loads all sessions stored in database. For each of them it will also create the respective {@link
     * TwitterHandler}.
     *
     * @return The number of new sessions loaded
     */
    public long loadAllSessions() {
        final long initialSize = this.loadedSessions.size();

        this.sessionRepository.findAll().forEach(this::loadSession);

        final long finalSize = this.loadedSessions.size();

        final List<String> sessionUsernames = this.loadedSessions.stream()
                                                                 .map(Session::getUserId)
                                                                 .collect(Collectors.toList());

        LOG.info(
                "Loaded {} Twitter sessions. Total loaded sessions so far is {}. Sessions : {}",
                finalSize - initialSize,
                finalSize,
                sessionUsernames
        );

        return finalSize - initialSize;
    }

    public void reloadAllSessions() {
        this.loadedSessions.clear();
        this.loadAllSessions();
    }

    protected void loadSession(final Session session) {
        final TwitterHandler handler = this.context.getBean(TwitterHandler.class);
        handler.registerAccessToken(session.getAccessToken());
        session.setTwitterHandler(handler);
        this.loadedSessions.add(session);
        LOG.debug("Setting current session to {}", session);
        this.currentSession.setValue(session);
    }

    public void addNewSession(final TwitterHandler twitterHandler) {
        final AccessToken accessToken = twitterHandler.getAccessToken();
        final Session session = new Session(
                accessToken.getScreenName(),
                accessToken,
                twitterHandler
        );

        this.loadSession(session);
        LOG.debug("Setting current session to {}", session);
        this.currentSession.setValue(session);
        this.saveAllSessions();
    }

    /**
     * Saves all sessions.
     */
    public void saveAllSessions() {
        this.loadedSessions.stream()
                           .peek(session -> LOG.info("Saving Twitter session : {}", session))
                           .forEach(this.sessionRepository::save);
        LOG.debug("Saved all sessions !");
    }
}