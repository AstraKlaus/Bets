package ak.spring.configs;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionInformation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

public class CustomSessionAuthenticationStrategy extends ConcurrentSessionControlAuthenticationStrategy {

    private final SessionRegistry sessionRegistry;

    public CustomSessionAuthenticationStrategy(SessionRegistry sessionRegistry) {
        super(sessionRegistry);
        this.sessionRegistry = sessionRegistry;
        setMaximumSessions(1);
        setExceptionIfMaximumExceeded(true);
    }

    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest request,
                                 HttpServletResponse response) throws SessionAuthenticationException {
        String sessionId = request.getSession().getId();
        List<SessionInformation> sessions = sessionRegistry.getAllSessions(authentication.getPrincipal(), false);

        if (sessions.size() >= 1) {
            // Завершаем существующие сессии
            for (SessionInformation sessionInfo : sessions) {
                sessionInfo.expireNow();
            }
            // Регистрируем новую сессию
            sessionRegistry.registerNewSession(sessionId, authentication.getPrincipal());
        } else {
            // Регистрируем новую сессию
            sessionRegistry.registerNewSession(sessionId, authentication.getPrincipal());
        }
    }
}

