package ak.spring.services;

import ak.spring.models.Person;
import ak.spring.repositories.PersonRepository;
import ak.spring.models.Subscription;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    public PersonService(PersonRepository personRepository, PasswordEncoder passwordEncoder) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Найти пользователя по username
    public Person findByUsername(String username) {
        return personRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь с таким именем не найден"));
    }

    // Получить всех пользователей
    public List<Person> getAllUsers() {
        return personRepository.findAll();
    }

    // Создать нового пользователя
    public Person createUser(Person person) {
        // Зашифровать пароль перед сохранением
        person.setPassword(passwordEncoder.encode(person.getPassword()));
        return personRepository.save(person);
    }

    // Обновить информацию о пользователе
    public Person updateUser(Person updatedPerson) {
        Optional<Person> existingPersonOpt = personRepository.findById(updatedPerson.getId());

        if (existingPersonOpt.isPresent()) {
            Person existingPerson = existingPersonOpt.get();
            existingPerson.setUsername(updatedPerson.getUsername());
            existingPerson.setPassword(passwordEncoder.encode(updatedPerson.getPassword()));
            existingPerson.setRole(updatedPerson.getRole());
            existingPerson.setSubscription(updatedPerson.getSubscription());
            return personRepository.save(existingPerson);
        } else {
            throw new RuntimeException("Пользователь не найден");
        }
    }

    // Удалить пользователя по ID
    public void deleteUser(Long id) {
        personRepository.deleteById(id);
    }

    // Назначить подписку пользователю
    public Person assignSubscription(Person person, Subscription subscription) {
        person.setSubscription(subscription);
        return personRepository.save(person);
    }

    // Удалить подписку у пользователя
    public Person removeSubscription(Person person) {
        person.setSubscription(null);
        return personRepository.save(person);
    }

    public void saveUser(Person person) {
        personRepository.save(person);
    }

    public void setSubscription(Person person, Subscription subscription) {
        person.setSubscription(subscription);
        personRepository.save(person);
    }
}
