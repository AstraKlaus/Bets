package ak.spring.controllers;

import ak.spring.models.Person;
import ak.spring.models.Subscription;
import ak.spring.services.PersonService;
import ak.spring.services.SubscriptionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final PersonService personService;

    public SubscriptionController(SubscriptionService subscriptionService, PersonService personService) {
        this.subscriptionService = subscriptionService;
        this.personService = personService;
    }

    @GetMapping("/account")
    public String account(Model model, Authentication authentication) {
        String username = authentication.getName();
        Person person = personService.findByUsername(username);

        // Получаем информацию о подписке
        Optional<Subscription> subscription = subscriptionService.getSubscriptionByPerson(person);  // Здесь нужно получить подписку пользователя из сервиса или репозитория
        model.addAttribute("user", person);
        if (subscription.isPresent()) {
            model.addAttribute("subscription", subscription.get());
        } else {
            model.addAttribute("message", "У вас нет активной подписки.");
        }

        // Если пользователь администратор, передаем список всех пользователей
        if (person.getRole().equals("ADMIN")) {
            List<Person> users = personService.getAllUsers();
            model.addAttribute("users", users);
        }

        return "account";
    }

    // Показать информацию о подписке текущего пользователя
    @GetMapping("/my")
    public String viewMySubscription(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Person person = personService.findByUsername(userDetails.getUsername());
        Optional<Subscription> subscription = subscriptionService.getSubscriptionByPerson(person);

        if (subscription.isPresent()) {
            model.addAttribute("subscription", subscription.get());
        } else {
            model.addAttribute("message", "У вас нет активной подписки.");
        }

        return "subscription/my_subscription";
    }

    // Продлить подписку для текущего пользователя
    @PostMapping("/extend")
    public String extendSubscription(@AuthenticationPrincipal UserDetails userDetails,
                                     @RequestParam int days,
                                     @RequestParam Double price,
                                     Model model) {
        Person person = personService.findByUsername(userDetails.getUsername());
        Optional<Subscription> subscriptionOpt = subscriptionService.getSubscriptionByPerson(person);

        if (subscriptionOpt.isPresent()) {
            Subscription subscription = subscriptionOpt.get();
            subscriptionService.extendSubscription(subscription, days, price);
            model.addAttribute("subscription", subscription);
            model.addAttribute("message", "Подписка успешно продлена.");
        } else {
            model.addAttribute("message", "У вас нет активной подписки.");
        }

        return "redirect:/subscriptions/my";
    }

    // Администратор: показать список всех подписок
    @GetMapping("/admin")
    public String viewAllSubscriptions(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Person admin = personService.findByUsername(userDetails.getUsername());

        if (!admin.getRole().equals("ADMIN")) {
            return "redirect:/dashboard"; // Перенаправить, если не администратор
        }

        List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
        model.addAttribute("subscriptions", subscriptions);

        return "subscription/all_subscriptions";
    }

    // Администратор: обновить подписку пользователя
    @PostMapping("/admin/update")
    public String updateSubscription(@RequestParam Long subscriptionId,
                                     @RequestParam LocalDate newEndDate,
                                     @RequestParam Double newPrice,
                                     Model model,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        Person admin = personService.findByUsername(userDetails.getUsername());

        if (!admin.getRole().equals("ADMIN")) {
            return "redirect:/dashboard"; // Перенаправить, если не администратор
        }

        Optional<Subscription> subscriptionOpt = subscriptionService.getAllSubscriptions()
                .stream()
                .filter(sub -> sub.getId().equals(subscriptionId))
                .findFirst();

        if (subscriptionOpt.isPresent()) {
            Subscription subscription = subscriptionOpt.get();
            subscriptionService.updateSubscription(subscription, newEndDate, newPrice);
            model.addAttribute("message", "Подписка обновлена.");
        } else {
            model.addAttribute("message", "Подписка не найдена.");
        }

        return "redirect:/subscriptions/admin";
    }

    // Администратор: удалить подписку
    @PostMapping("/admin/delete")
    public String deleteSubscription(@RequestParam Long subscriptionId,
                                     Model model,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        Person admin = personService.findByUsername(userDetails.getUsername());

        if (!admin.getRole().equals("ADMIN")) {
            return "redirect:/dashboard"; // Перенаправить, если не администратор
        }

        subscriptionService.deleteSubscription(subscriptionId);
        model.addAttribute("message", "Подписка удалена.");

        return "redirect:/subscriptions/admin";
    }
}
