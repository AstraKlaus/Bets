package ak.spring.controllers;

import ak.spring.models.Person;
import ak.spring.services.PersonService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



@CrossOrigin(origins = "wss://blbet.fun")
@Controller
public class WebSocketDataController {

    private WebSocketSession pythonSession;
    private final PersonService personService;

    private final List<Map<String, Object>> bets = new ArrayList<>();
    private boolean isUpdating = true;


    public WebSocketDataController(SimpMessagingTemplate messagingTemplate, PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpServletResponse response, Principal principal) {
        Person person = personService.findByUsername(principal.getName());

        if (person == null || person.getSubscription() == null || !person.getSubscription().isActive()) {
            return "redirect:/subscriptions/account"; // Перенаправление на страницу подписок
        }
        model.addAttribute("bets", bets); // Передаем текущие ставки в модель
        model.addAttribute("sendingData", isUpdating); // Передаем состояние флага в модель
        return "dashboard";
    }

    // Прием сообщений от Python-сервера
    @MessageMapping("/receiveBet")
    @SendTo("/topic/bets")
    public List<Map<String, Object>> receiveBetsFromPython(@Payload List<Map<String, Object>> newBets) {
        System.out.println("Received bets: " + newBets);

        // Очищаем текущий список ставок и добавляем новые
        bets.clear();
        bets.addAll(newBets);

        return bets; // Отправляем обновленный список ставок всем подписчикам
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

