package ak.spring.exceptions;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public CustomAuthenticationFailureHandler() {
        // Установите URL по умолчанию при ошибке
        super("/login?error=true");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        if (exception instanceof SessionAuthenticationException) {
            // Перенаправление при превышении максимального количества сессий
            getRedirectStrategy().sendRedirect(request, response, "/login?sessionExpired=true");
        } else {
            // Стандартная обработка остальных ошибок
            super.onAuthenticationFailure(request, response, exception);
        }
    }
}
