package hexlet.code.app.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.service.UserService;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
@Profile("!test")
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var data = new UserCreateDTO();
        data.setEmail("hexlet@example.com");
        data.setPassword("qwerty");
        userService.create(data);
    }
}
