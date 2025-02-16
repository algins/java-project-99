package hexlet.code.controller.api;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TasksControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    private JwtRequestPostProcessor token;
    private Task task;
    private User user;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
            .apply(springSecurity())
            .build();

        user = Instancio.of(modelGenerator.getUserModel()).create();
        token = jwt().jwt(builder -> builder.subject(user.getEmail()));
        task = Instancio.of(modelGenerator.getTaskModel()).create();
    }

    @Test
    public void testIndex() throws Exception {
        taskRepository.save(task);
        var request = get("/api/tasks").with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("[0].id").isEqualTo(task.getId());
            v.node("[0].index").isEqualTo(task.getIndex());
            v.node("[0].createdAt").isEqualTo(task.getCreatedAt().toString());
            v.node("[0].assignee_id").isEqualTo(task.getAssignee().getId());
            v.node("[0].title").isEqualTo(task.getName());
            v.node("[0].content").isEqualTo(task.getDescription());
            v.node("[0].status").isEqualTo(task.getTaskStatus().getSlug());
            v.node("[0].taskLabelIds").isEqualTo(task.getLabels().stream().map(Label::getId).toList());
        });
    }

    @Test
    public void testIndexWithFilters() throws Exception {
        taskRepository.save(task);
        var titleCont = task.getName().substring(1);
        var assigneeId = task.getAssignee().getId();
        var status = task.getTaskStatus().getSlug();
        var labelId = task.getLabels().stream().map(Label::getId).toList().get(0);

        var url = String.join("",
            "/api/tasks?titleCont=" + titleCont,
            "&assigneeId=" + assigneeId,
            "&status=" + status,
            "&labelId=" + labelId
        );

        var request = get(url).with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body)
            .isArray()
            .allSatisfy(element -> assertThatJson(element)
                .and(v -> v.node("title").asString().containsIgnoringCase(titleCont))
                .and(v -> v.node("assignee_id").isEqualTo(assigneeId))
                .and(v -> v.node("status").isEqualTo(status))
                .and(v -> v.node("taskLabelIds").isArray().contains(labelId))
        );
    }

    @Test
    public void testShow() throws Exception {
        taskRepository.save(task);
        var request = get("/api/tasks/" + task.getId()).with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("id").isEqualTo(task.getId());
            v.node("index").isEqualTo(task.getIndex());
            v.node("createdAt").isEqualTo(task.getCreatedAt().toString());
            v.node("assignee_id").isEqualTo(task.getAssignee().getId());
            v.node("title").isEqualTo(task.getName());
            v.node("content").isEqualTo(task.getDescription());
            v.node("status").isEqualTo(task.getTaskStatus().getSlug());
            v.node("taskLabelIds").isEqualTo(task.getLabels().stream().map(Label::getId).toList());
        });
    }

    @Test
    public void testCreate() throws Exception {
        var data = Map.of(
            "index", task.getIndex(),
            "assignee_id", task.getAssignee().getId(),
            "title", task.getName(),
            "content", task.getDescription(),
            "status", task.getTaskStatus().getSlug(),
            "taskLabelIds", task.getLabels().stream().map(Label::getId).toList()
        );

        var request = post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isCreated())
            .andReturn();

        var createdTask = taskRepository.findByName((String) data.get("title")).get();

        assertThat(data.get("index")).isEqualTo(createdTask.getIndex());
        assertThat(data.get("assignee_id")).isEqualTo(createdTask.getAssignee().getId());
        assertThat(data.get("title")).isEqualTo(createdTask.getName());
        assertThat(data.get("content")).isEqualTo(createdTask.getDescription());
        assertThat(data.get("status")).isEqualTo(createdTask.getTaskStatus().getSlug());
        assertThat(data.get("taskLabelIds")).isEqualTo(createdTask.getLabels().stream().map(Label::getId).toList());

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("id").isEqualTo(createdTask.getId());
            v.node("index").isEqualTo(createdTask.getIndex());
            v.node("createdAt").isEqualTo(createdTask.getCreatedAt().toString());
            v.node("assignee_id").isEqualTo(createdTask.getAssignee().getId());
            v.node("title").isEqualTo(createdTask.getName());
            v.node("content").isEqualTo(createdTask.getDescription());
            v.node("status").isEqualTo(createdTask.getTaskStatus().getSlug());
            v.node("taskLabelIds").isEqualTo(createdTask.getLabels().stream().map(Label::getId).toList());
        });
    }

    @Test
    public void testCreateWithInvalidData() throws Exception {
        var data = Map.of(
            "title", task.getName()
        );

        var request = post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        mockMvc.perform(request)
            .andExpect(status().isBadRequest());

        var createdTask = taskRepository.findByName(data.get("title")).orElse(null);
        assertNull(createdTask);
    }

    @Test
    public void testUpdate() throws Exception {
        taskRepository.save(task);

        var newAssignee = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newAssignee);
        var newTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(newTaskStatus);
        var newLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(newLabel);

        var data = Map.of(
            "index", 12,
            "assignee_id", newAssignee.getId(),
            "title", "Test title",
            "content", "Test content",
            "status", newTaskStatus.getSlug(),
            "taskLabelIds", List.of(newLabel.getId())
        );

        var request = put("/api/tasks/" + task.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        var updatedTask = taskRepository.findById(task.getId()).get();

        assertThat(data.get("index")).isEqualTo(updatedTask.getIndex());
        assertThat(data.get("assignee_id")).isEqualTo(updatedTask.getAssignee().getId());
        assertThat(data.get("title")).isEqualTo(updatedTask.getName());
        assertThat(data.get("content")).isEqualTo(updatedTask.getDescription());
        assertThat(data.get("status")).isEqualTo(updatedTask.getTaskStatus().getSlug());
        assertThat(data.get("taskLabelIds")).isEqualTo(updatedTask.getLabels().stream().map(Label::getId).toList());

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("id").isEqualTo(updatedTask.getId());
            v.node("index").isEqualTo(updatedTask.getIndex());
            v.node("createdAt").isEqualTo(updatedTask.getCreatedAt().toString());
            v.node("assignee_id").isEqualTo(updatedTask.getAssignee().getId());
            v.node("title").isEqualTo(updatedTask.getName());
            v.node("content").isEqualTo(updatedTask.getDescription());
            v.node("status").isEqualTo(updatedTask.getTaskStatus().getSlug());
            v.node("taskLabelIds").isEqualTo(updatedTask.getLabels().stream().map(Label::getId).toList());
        });
    }

    @Test
    public void testUpdateWithInvalidData() throws Exception {
        taskRepository.save(task);

        var data = Map.of(
            "title", ""
        );

        var request = put("/api/tasks/" + task.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        mockMvc.perform(request)
            .andExpect(status().isBadRequest());

        var updatedTask = taskRepository.findById(task.getId()).get();
        assertThat(task.getName()).isEqualTo(updatedTask.getName());
    }

    @Test
    public void testDestroy() throws Exception {
        taskRepository.save(task);
        var request = delete("/api/tasks/" + task.getId()).with(token);

        mockMvc.perform(request)
            .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(task.getId())).isFalse();
    }
}
