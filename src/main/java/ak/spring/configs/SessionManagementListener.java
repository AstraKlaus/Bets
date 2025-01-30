package ak.spring.configs;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

import java.util.List;


import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManagementListener implements HttpSessionListener {

    private final SessionRegistry sessionRegistry;

    // Потокобезопасное хранилище активных сессий
    private final ConcurrentHashMap<String, HttpSession> activeSessions = new ConcurrentHashMap<>();

    public SessionManagementListener(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * Сохранение активной сессии при её создании.
     */
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        activeSessions.put(session.getId(), session);
        session.setAttribute("lastActivity", System.currentTimeMillis());
        System.out.println("Session created: " + session.getId());
    }

    /**
     * Удаление сессии из локального хранилища при её уничтожении.
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        activeSessions.remove(session.getId());
        System.out.println("Session destroyed: " + session.getId());
    }

    /**
     * Проверка и очистка истекших сессий.
     * Использует SessionRegistry для синхронизации с информацией Spring Security.
     */
    @Scheduled(fixedRate = 30000) // Проверка каждые 30 секунд
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();

        // Получаем всех пользователей с активными сессиями
        List<Object> principals = sessionRegistry.getAllPrincipals();
        for (Object principal : principals) {
            // Получаем все сессии пользователя
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
            for (SessionInformation sessionInfo : sessions) {
                // Проверяем, истекла ли сессия
                if (sessionInfo.isExpired()) {
                    System.out.println("Removing expired session: " + sessionInfo.getSessionId());
                    sessionRegistry.removeSessionInformation(sessionInfo.getSessionId());
                } else {
                    // Дополнительно проверяем время последней активности
                    HttpSession session = activeSessions.get(sessionInfo.getSessionId());
                    if (session != null) {
                        Long lastActivity = (Long) session.getAttribute("lastActivity");
                        if (lastActivity != null && now - lastActivity > 60000) { // 60 секунд неактивности
                            System.out.println("Invalidating inactive session: " + session.getId());
                            session.invalidate();
                            sessionRegistry.removeSessionInformation(session.getId());
                        }
                    }
                }
            }
        }
    }

    /**
     * Получение всех активных сессий.
     */
    public List<SessionInformation> getAllActiveSessions() {
        return sessionRegistry.getAllPrincipals().stream()
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .toList();
    }

    /**
     * Завершение сессии по её ID.
     */
    public void expireSession(String sessionId) {
        sessionRegistry.getAllPrincipals().forEach(principal -> {
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
            for (SessionInformation sessionInfo : sessions) {
                if (sessionInfo.getSessionId().equals(sessionId)) {
                    System.out.println("Expiring session manually: " + sessionId);
                    sessionInfo.expireNow();
                    activeSessions.remove(sessionId);
                }
            }
        });
    }
}

