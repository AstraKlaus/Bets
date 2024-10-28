package ak.spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Статус подписки
    @Column(nullable = false)
    private boolean status;

    // Дата начала подписки
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // Дата окончания подписки
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // Цена подписки
    @Column(nullable = false)
    private Double price;

    // Связь с пользователем (один к одному)
    @OneToOne(mappedBy = "subscription")
    private Person person;

    // Метод для продления подписки
    public void extendSubscription(int days, Double newPrice) {
        this.endDate = this.endDate.plusDays(days);  // Продлеваем на заданное количество дней
        this.price = newPrice;  // Устанавливаем новую цену
        this.status = true;  // Обновляем статус
    }

    // Метод для проверки, активна ли подписка
    public boolean isActive() {
        return LocalDate.now().isBefore(endDate);  // Проверяем, что текущая дата меньше даты окончания
    }
}
