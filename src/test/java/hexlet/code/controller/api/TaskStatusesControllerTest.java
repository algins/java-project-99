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

import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.util.ModelGenerator;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TaskStatusesControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    private JwtRequestPostProcessor token;
    private User user;
    private TaskStatus taskStatus;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
            .apply(springSecurity())
            .build();

        user = Instancio.of(modelGenerator.getUserModel()).create();
        token = jwt().jwt(builder -> builder.subject(user.getEmail()));
        taskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
    }

    @Test
    public void testIndex() throws Exception {
        taskStatusRepository.save(taskStatus);
        var request = get("/api/task_statuses").with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("[0].id").isEqualTo(taskStatus.getId());
            v.node("[0].name").isEqualTo(taskStatus.getName());
            v.node("[0].slug").isEqualTo(taskStatus.getSlug());
            v.node("[0].createdAt").isEqualTo(taskStatus.getCreatedAt().toString());
        });
    }

    @Test
    public void testShow() throws Exception {
        taskStatusRepository.save(taskStatus);
        var request = get("/api/task_statuses/" + taskStatus.getId()).with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("id").isEqualTo(taskStatus.getId());
            v.node("name").isEqualTo(taskStatus.getName());
            v.node("slug").isEqualTo(taskStatus.getSlug());
            v.node("createdAt").isEqualTo(taskStatus.getCreatedAt().toString());
        });
    }

    @Test
    public void testCreate() throws Exception {
        var data = Map.of(
            "name", "New",
            "slug", "new"
        );

        var request = post("/api/task_statuses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isCreated())
            .andReturn();

        var createdTaskStatus = taskStatusRepository.findBySlug(data.get("slug")).get();

        assertThat(data.get("name")).isEqualTo(createdTaskStatus.getName());
        assertThat(data.get("slug")).isEqualTo(createdTaskStatus.getSlug());

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("id").isEqualTo(createdTaskStatus.getId());
            v.node("name").isEqualTo(createdTaskStatus.getName());
            v.node("slug").isEqualTo(createdTaskStatus.getSlug());
            v.node("createdAt").isEqualTo(createdTaskStatus.getCreatedAt().toString());
        });
    }

    @Test
    public void testCreateWithInvalidData() throws Exception {
        var data = Map.of(
            "slug", "new"
        );

        var request = post("/api/task_statuses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        mockMvc.perform(request)
            .andExpect(status().isBadRequest());

        var createdTaskStatus = taskStatusRepository.findBySlug(data.get("slug")).orElse(null);
        assertNull(createdTaskStatus);
    }

    @Test
    public void testCreateWithDuplicateSlug() throws Exception {
        taskStatusRepository.save(taskStatus);

        var data = Map.of(
            "name", "New",
            "slug", taskStatus.getSlug()
        );

        var request = post("/api/task_statuses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        mockMvc.perform(request)
            .andExpect(status().isBadRequest());

        var createdTaskStatus = taskStatusRepository.findByName(data.get("name")).orElse(null);
        assertNull(createdTaskStatus);
    }

    @Test
    public void testUpdate() throws Exception {
        taskStatusRepository.save(taskStatus);

        var data = Map.of(
            "name", "newStatus"
        );

        var request = put("/api/task_statuses/" + taskStatus.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        var updatedTaskStatus = taskStatusRepository.findById(taskStatus.getId()).get();

        assertThat(data.get("name")).isEqualTo(updatedTaskStatus.getName());

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("id").isEqualTo(updatedTaskStatus.getId());
            v.node("name").isEqualTo(updatedTaskStatus.getName());
            v.node("slug").isEqualTo(updatedTaskStatus.getSlug());
            v.node("createdAt").isEqualTo(updatedTaskStatus.getCreatedAt().toString());
        });
    }

    @Test
    public void testUpdateWithInvalidData() throws Exception {
        taskStatusRepository.save(taskStatus);

        var data = Map.of(
            "name", ""
        );

        var request = put("/api/task_statuses/" + taskStatus.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        mockMvc.perform(request)
            .andExpect(status().isBadRequest());

        var updatedTaskStatus = taskStatusRepository.findById(taskStatus.getId()).get();
        assertThat(taskStatus.getName()).isEqualTo(updatedTaskStatus.getName());
    }

    @Test
    public void testDestroy() throws Exception {
        taskStatusRepository.save(taskStatus);
        var request = delete("/api/task_statuses/" + taskStatus.getId()).with(token);

        mockMvc.perform(request)
            .andExpect(status().isNoContent());

        assertThat(taskStatusRepository.existsById(taskStatus.getId())).isFalse();
    }
}
