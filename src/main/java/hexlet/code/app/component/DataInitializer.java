package hexlet.code.app.component;

import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import hexlet.code.app.dto.TaskStatusCreateDTO;
import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.service.TaskStatusService;
import hexlet.code.app.service.UserService;
import lombok.RequiredArgsConstructor;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final TaskStatusService taskStatusService;
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeDefaultUser();
        initializeTaskStatuses();
    }

    private void initializeDefaultUser() {
        UserCreateDTO userData = new UserCreateDTO();
        userData.setEmail("hexlet@example.com");
        userData.setPassword("qwerty");
        userService.create(userData);
    }

    private void initializeTaskStatuses() {
        Map<String, String> statuses = Map.of(
            "draft", "Draft",
            "to_review", "To review",
            "to_be_fixed", "To be fixed",
            "to_publish", "To publish",
            "published", "Published"
        );

        statuses.forEach((slug, name) -> {
            TaskStatusCreateDTO taskStatusData = new TaskStatusCreateDTO();
            taskStatusData.setName(name);
            taskStatusData.setSlug(slug);
            taskStatusService.create(taskStatusData);
        });
    }
}
