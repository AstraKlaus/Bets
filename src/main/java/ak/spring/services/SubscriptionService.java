package ak.spring.services;

import ak.spring.models.Person;
import ak.spring.models.Subscription;
import ak.spring.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    // Метод для получения всех подписок
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    // Получить подписку по id пользователя
    public Optional<Subscription> getSubscriptionByPerson(Person person) {
        return subscriptionRepository.findByPersonId(person.getId());
    }

    // Создание новой подписки
    public Subscription createSubscription(Person person, LocalDate startDate, LocalDate endDate, Double price) {
        Subscription subscription = new Subscription();
        subscription.setPerson(person);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setPrice(price);
        subscription.setStatus(true);
        return subscriptionRepository.save(subscription);
    }

    // Продление подписки
    public Subscription extendSubscription(Subscription subscription, int days, Double newPrice) {
        subscription.extendSubscription(days, newPrice);
        return subscriptionRepository.save(subscription);
    }

    // Проверка, активна ли подписка
    public boolean isSubscriptionActive(Subscription subscription) {
        return subscription.isActive();
    }

    // Обновление подписки (например, изменение цены, дат)
    public Subscription updateSubscription(Subscription subscription, LocalDate newEndDate, Double newPrice) {
        subscription.setEndDate(newEndDate);
        subscription.setPrice(newPrice);
        return subscriptionRepository.save(subscription);
    }

    // Удаление подписки
    public void deleteSubscription(Long id) {
        subscriptionRepository.deleteById(id);
    }
}
