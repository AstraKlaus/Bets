package ak.spring.controllers;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@CrossOrigin(origins = "ws://localhost:8084")
@Controller
public class WebSocketDataController {

    private WebSocketSession pythonSession;

    private final List<Map<String, Object>> bets = new ArrayList<>();
    private boolean isUpdating = true;

    @Autowired
    public WebSocketDataController(SimpMessagingTemplate messagingTemplate) {
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("bets", bets); // Передаем текущие ставки в модель
        model.addAttribute("sendingData", isUpdating); // Передаем состояние флага в модель
        return "dashboard";
    }

    // Прием сообщений от Python-сервера
    @MessageMapping("/receiveBet")
    @SendTo("/topic/bets")
    public Map<String, Object> receiveBetFromPython(@Payload Map<String, Object> bet) {
        System.out.println("Received bet: " + bet);
        bets.add(0, bet); // Сохраняем полученную ставку в список (опционально)
        return bet;       // Отправляем ставку всем подписчикам
    }

    @MessageMapping("/stopParser")
    public void stopParser() {
        sendCommandToPython("stop");  // Отправляем команду на остановку парсера
        System.out.println("Parser stop command sent to Python");
    }

    @MessageMapping("/startParser")
    public void startParser() {
        sendCommandToPython("start");  // Отправляем команду на запуск парсера
        System.out.println("Parser start command sent to Python");
    }

    @MessageMapping("/stopUpdates")
    public void stopUpdates() {
        isUpdating = false; // Останавливаем обновление
    }

    @MessageMapping("/startUpdates")
    public void startUpdates() {
        isUpdating = true; // Запускаем обновление
    }

    public void sendCommandToPython(String command) {
        if (pythonSession != null && pythonSession.isOpen()) {
            try {
                // Отправляем команду на Python сервер через WebSocket
                pythonSession.sendMessage(new TextMessage(command));
                System.out.println("Command sent to Python: " + command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Python WebSocket session is not open");
        }
    }
}

