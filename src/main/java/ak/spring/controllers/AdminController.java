package ak.spring.controllers;

import ak.spring.models.Person;
import ak.spring.models.Subscription;
import ak.spring.services.PersonService;
import ak.spring.services.SubscriptionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {


    private final PersonService personService;
    private final SubscriptionService subscriptionService;

    public AdminController(PersonService personService, SubscriptionService subscriptionService
                           ) {
        this.personService = personService;
        this.subscriptionService = subscriptionService;
    }

    // Отображение личного кабинета пользователя


    @PostMapping("/subscribe")
    public String subscribe(@RequestParam String username,
                            @RequestParam LocalDate startDate,
                            @RequestParam LocalDate endDate,
                            Model model) {
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            model.addAttribute("error", "Дата окончания должна быть позже даты начала.");
            return "redirect:/subscriptions/account"; // Возвращаем на страницу с ошибкой
        }

        Person person = personService.findByUsername(username); // Получаем пользователя по ID

        Optional<Subscription> pastSubscription = subscriptionService.getSubscriptionByPerson(person);
        // Создаем подписку
        personService.setSubscription(person, subscriptionService.createSubscription(person, startDate, endDate, 500.0));

        pastSubscription.ifPresent(subscription -> subscriptionService.deleteSubscription(subscription.getId()));
        // Перенаправляем на страницу с пользователями или отображаем успешное сообщение
        return "redirect:/subscriptions/account";
    }

    // Метод для отмены подписки
    @PostMapping("/cancel-subscription/{username}")
    public String cancelSubscription(@PathVariable String username, Model model) {
        Person person = personService.findByUsername(username);
        Optional<Subscription> subscription = subscriptionService.getSubscriptionByPerson(personService.findByUsername(username));


        subscription.ifPresent(value -> {
            // Убираем ссылку на подписку у пользователя
            person.setSubscription(null);
            personService.saveUser(person);  // Сохраняем изменения в пользователе

            // Удаляем саму подписку
            subscriptionService.deleteSubscription(value.getId());
        });

        // Перенаправляем на страницу с пользователями или отображаем успешное сообщение
        return "redirect:/subscriptions/account";
    }

    @PostMapping("/edit-user")
    public String editUser(@ModelAttribute("person") Person person, @RequestParam("password") String password) {
        // Обновление пользователя в базе данных
        personService.updateUser(person, password);
        return "redirect:/account"; // Перенаправление на страницу управления пользователями
    }
}

