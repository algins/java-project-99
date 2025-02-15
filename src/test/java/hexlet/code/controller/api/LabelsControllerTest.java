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

import hexlet.code.model.Label;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.util.ModelGenerator;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LabelsControllerTest {

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

    private JwtRequestPostProcessor token;
    private User user;
    private Label label;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
            .apply(springSecurity())
            .build();

        user = Instancio.of(modelGenerator.getUserModel()).create();
        token = jwt().jwt(builder -> builder.subject(user.getEmail()));
        label = Instancio.of(modelGenerator.getLabelModel()).create();
    }

    @Test
    public void testIndex() throws Exception {
        labelRepository.save(label);
        var request = get("/api/labels").with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("[0].id").isEqualTo(label.getId());
            v.node("[0].name").isEqualTo(label.getName());
            v.node("[0].createdAt").isEqualTo(label.getCreatedAt().toString());
        });
    }

    @Test
    public void testShow() throws Exception {
        labelRepository.save(label);
        var request = get("/api/labels/" + label.getId()).with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("id").isEqualTo(label.getId());
            v.node("name").isEqualTo(label.getName());
            v.node("createdAt").isEqualTo(label.getCreatedAt().toString());
        });
    }

    @Test
    public void testCreate() throws Exception {
        var data = Map.of(
            "name", "new label"
        );

        var request = post("/api/labels")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isCreated())
            .andReturn();

        var createdLabel = labelRepository.findByName(data.get("name")).get();

        assertThat(data.get("name")).isEqualTo(createdLabel.getName());

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("id").isEqualTo(createdLabel.getId());
            v.node("name").isEqualTo(createdLabel.getName());
            v.node("createdAt").isEqualTo(createdLabel.getCreatedAt().toString());
        });
    }

    @Test
    public void testCreateWithInvalidData() throws Exception {
        var data = Map.of(
            "name", "n"
        );

        var request = post("/api/labels")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        mockMvc.perform(request)
            .andExpect(status().isBadRequest());

        var createdLabel = labelRepository.findByName(data.get("name")).orElse(null);
        assertNull(createdLabel);
    }

    @Test
    public void testCreateWithDuplicateName() throws Exception {
        labelRepository.save(label);

        var data = Map.of(
            "name", label.getName()
        );

        var request = post("/api/labels")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        mockMvc.perform(request)
            .andExpect(status().isBadRequest());

        var count = labelRepository.countByName(data.get("name"));
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testUpdate() throws Exception {
        labelRepository.save(label);

        var data = Map.of(
            "name", "Bug"
        );

        var request = put("/api/labels/" + label.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        var updatedLabel = labelRepository.findById(label.getId()).get();

        assertThat(data.get("name")).isEqualTo(updatedLabel.getName());

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("id").isEqualTo(updatedLabel.getId());
            v.node("name").isEqualTo(updatedLabel.getName());
            v.node("createdAt").isEqualTo(updatedLabel.getCreatedAt().toString());
        });
    }

    @Test
    public void testUpdateWithInvalidData() throws Exception {
        labelRepository.save(label);

        var data = Map.of(
            "name", "n"
        );

        var request = put("/api/labels/" + label.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        mockMvc.perform(request)
            .andExpect(status().isBadRequest());

        var updatedLabel = labelRepository.findById(label.getId()).get();
        assertThat(label.getName()).isEqualTo(updatedLabel.getName());
    }

    @Test
    public void testDestroy() throws Exception {
        labelRepository.save(label);
        var request = delete("/api/labels/" + label.getId()).with(token);

        mockMvc.perform(request)
            .andExpect(status().isNoContent());

        assertThat(labelRepository.existsById(label.getId())).isFalse();
    }
}
