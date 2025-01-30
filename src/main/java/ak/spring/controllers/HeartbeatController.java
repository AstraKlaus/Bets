package ak.spring.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/heartbeat")
public class HeartbeatController {

    @PostMapping
    public ResponseEntity<Void> handleHeartbeat(HttpServletRequest request) {
        // Получаем текущую сессию
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Обновляем атрибут сессии с последним временем активности
            session.setAttribute("lastActivity", System.currentTimeMillis());
        }
        return ResponseEntity.ok().build();
    }
}

