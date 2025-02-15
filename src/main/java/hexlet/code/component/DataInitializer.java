package hexlet.code.component;

import java.util.List;
import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.service.LabelService;
import hexlet.code.service.TaskStatusService;
import hexlet.code.service.UserService;
import lombok.RequiredArgsConstructor;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final LabelService labelService;
    private final TaskStatusService taskStatusService;
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeDefaultUser();
        initializeTaskStatuses();
        initializeLabels();
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

    private void initializeLabels() {
        List<String> labels = List.of(
            "bug",
            "feature"
        );

        labels.forEach(name -> {
            LabelCreateDTO labelData = new LabelCreateDTO();
            labelData.setName(name);
            labelService.create(labelData);
        });
    }
}
