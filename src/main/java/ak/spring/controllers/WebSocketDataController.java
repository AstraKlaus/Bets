package ak.spring.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
public class WebSocketDataController {

    private final SimpMessagingTemplate messagingTemplate;
    private final List<Map<String, Object>> bets = new ArrayList<>(); // Хранение ставок в памяти
    private boolean isUpdating = true;

    public WebSocketDataController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("bets", bets); // Передаем текущие ставки в модель
        model.addAttribute("sendingData", isUpdating);// Передаем состояние флага в модель
        return "dashboard"; // Возвращаем шаблон dashboard
    }

    @Scheduled(fixedRate = 1000) // Генерация данных каждую секунду
    public void sendRandomData() {
        if (isUpdating) { // Проверяем, можно ли отправлять данные
            Map<String, Object> newBet = generateRandomBet(); // Генерируем одну новую ставку
            bets.add(0, newBet); // Добавляем новую ставку в начало списка
            messagingTemplate.convertAndSend("/topic/bets", newBet); // Отправляем только новую ставку всем подписчикам
        }
    }

    private Map<String, Object> generateRandomBet() {
        Map<String, Object> bet = new HashMap<>();
        Random random = new Random();
        String[] games = {"⚽", "🏀", "🎾", "🏒"};
        String[] bookmakers = {"Zenit", "Coral", "Vbet", "Betfair sport"};

        bet.put("game", games[random.nextInt(games.length)]);
        bet.put("percentage", 80 + random.nextDouble() * 40); // Процент от 80 до 120
        bet.put("bookmakers", List.of(bookmakers[random.nextInt(bookmakers.length)], bookmakers[random.nextInt(bookmakers.length)]));
        bet.put("events", List.of("Событие скрыто", "Событие скрыто"));
        bet.put("odds", List.of(random.nextDouble() * 5, random.nextDouble() * 5));
        bet.put("bet", "xxx");

        return bet;
    }

    @MessageMapping("/stopUpdates")
    public void stopUpdates() {
        isUpdating = false; // Останавливаем обновление
    }

    // Метод для продолжения обновления
    @MessageMapping("/startUpdates")
    public void startUpdates() {
        isUpdating = true; // Запускаем обновление
    }
}

