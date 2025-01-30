package ak.spring.configs;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionCleanupTask {

    private final SessionRegistry sessionRegistry;

    public SessionCleanupTask(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Scheduled(fixedRate = 60000) // Проверяем раз в минуту
    public void cleanupExpiredSessions() {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
            for (SessionInformation session : sessions) {
                if (session.getLastRequest().getTime() + 60000 < System.currentTimeMillis()) {
                    session.expireNow();
                }
            }
        }
    }
}

