package hexlet.code.util;

import java.util.ArrayList;
import java.util.List;

import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.datafaker.Faker;

@Getter
@Component
public class ModelGenerator {
    private Model<Label> labelModel;
    private Model<Task> taskModel;
    private Model<TaskStatus> taskStatusModel;
    private Model<User> userModel;

    @Autowired
    private Faker faker;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    private void init() {
        taskStatusModel = Instancio.of(TaskStatus.class)
            .ignore(Select.field(TaskStatus::getId))
            .ignore(Select.field(TaskStatus::getTasks))
            .supply(Select.field(TaskStatus::getName), () -> faker.lorem().word())
            .supply(Select.field(TaskStatus::getSlug), () -> faker.lorem().word())
            .toModel();

        userModel = Instancio.of(User.class)
            .ignore(Select.field(User::getId))
            .ignore(Select.field(User::getTasks))
            .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
            .supply(Select.field(User::getLastName), () -> faker.name().lastName())
            .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
            .supply(Select.field(User::getPasswordDigest), () -> {
                var password = faker.internet().password(3, 100);
                return passwordEncoder.encode(password);
            })
            .toModel();

        labelModel = Instancio.of(Label.class)
            .ignore(Select.field(Label::getId))
            .ignore(Select.field(Label::getTasks))
            .supply(Select.field(Label::getName), () -> faker.lorem().sentence())
            .toModel();

        taskModel = Instancio.of(Task.class)
            .ignore(Select.field(Task::getId))
            .supply(Select.field(Task::getAssignee), () -> {
                var user = Instancio.of(getUserModel()).create();
                userRepository.save(user);
                return user;
            })
            .supply(Select.field(Task::getIndex), () -> faker.number().randomDigit())
            .supply(Select.field(Task::getName), () -> faker.lorem().word())
            .supply(Select.field(Task::getDescription), () -> faker.lorem().word())
            .supply(Select.field(Task::getTaskStatus), () -> {
                var taskStatus = Instancio.of(getTaskStatusModel()).create();
                taskStatusRepository.save(taskStatus);
                return taskStatus;
            })
            .supply(Select.field(Task::getLabels), () -> {
                var label = Instancio.of(getLabelModel()).create();
                labelRepository.save(label);
                return new ArrayList<>(List.of(label));
            })
            .toModel();
    }
}
