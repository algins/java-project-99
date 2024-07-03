package hexlet.code.app.util;

import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import hexlet.code.app.model.User;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.datafaker.Faker;

@Getter
@Component
public class ModelGenerator {
    private Model<User> userModel;

    @Autowired
    private Faker faker;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    private void init() {
        userModel = Instancio.of(User.class)
            .ignore(Select.field(User::getId))
            .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
            .supply(Select.field(User::getLastName), () -> faker.name().lastName())
            .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
            .supply(Select.field(User::getPasswordDigest), () -> {
                var password = faker.internet().password(3, 100);
                return passwordEncoder.encode(password);
            })
            .toModel();
    }
}
