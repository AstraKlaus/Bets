package ak.spring.controllers;


import ak.spring.repositories.PersonRepository;
import ak.spring.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping
public class AuthController {

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(PersonRepository personRepository, PasswordEncoder passwordEncoder) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Отображение страницы логина
    @GetMapping("/login")
    public String login() {
        return "login";  // Здесь предполагается, что у вас есть соответствующий шаблон login.html
    }

    // Отображение страницы регистрации
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("person", new Person());
        return "register";  // Здесь предполагается, что у вас есть шаблон register.html
    }

    // Обработка формы регистрации
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("person") Person person, Model model) {
        // Проверяем, не существует ли уже пользователь с таким именем
        if (personRepository.findByUsername(person.getUsername()).isPresent()) {
            model.addAttribute("error", "Пользователь с таким именем уже существует");
            return "register";
        }

        // Хэшируем пароль и сохраняем пользователя в базе данных
        person.setPassword(passwordEncoder.encode(person.getPassword()));
        person.setRole("USER");  // Устанавливаем роль по умолчанию
        personRepository.save(person);

        // После успешной регистрации перенаправляем на страницу логина
        return "redirect:/login";
    }


    @PostMapping("/login")
    public String loginPerson(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", true);
        }
        return "login";
    }

    @GetMapping("/")
    public String redirectToDashboard() {
        return "redirect:/dashboard";
    }

}
