package hexlet.code.app.controller.api;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelGenerator;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UsersControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    private JwtRequestPostProcessor token;
    private User user;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
            .apply(springSecurity())
            .build();

        user = Instancio.of(modelGenerator.getUserModel()).create();
        token = jwt().jwt(builder -> builder.subject(user.getEmail()));
    }

    @Test
    public void testIndex() throws Exception {
        userRepository.save(user);
        var request = get("/api/users").with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("[0].id").isEqualTo(user.getId());
            v.node("[0].email").isEqualTo(user.getEmail());
            v.node("[0].firstName").isEqualTo(user.getFirstName());
            v.node("[0].lastName").isEqualTo(user.getLastName());
            v.node("[0].createdAt").isEqualTo(user.getCreatedAt());
        });
    }

    @Test
    public void testShow() throws Exception {
        userRepository.save(user);
        var request = get("/api/users/" + user.getId()).with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("id").isEqualTo(user.getId());
            v.node("email").isEqualTo(user.getEmail());
            v.node("firstName").isEqualTo(user.getFirstName());
            v.node("lastName").isEqualTo(user.getLastName());
            v.node("createdAt").isEqualTo(user.getCreatedAt());
        });
    }

    @Test
    public void testCreate() throws Exception {
        var data = Map.of(
            "email", "jack@google.com",
            "firstName", "Jack",
            "lastName", "Jons",
            "password", "some-password"
        );

        var request = post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isCreated())
            .andReturn();

        var createdUser = userRepository.findByEmail(data.get("email")).get();

        assertThat(data.get("email")).isEqualTo(createdUser.getEmail());
        assertThat(data.get("firstName")).isEqualTo(createdUser.getFirstName());
        assertThat(data.get("lastName")).isEqualTo(createdUser.getLastName());
        assertThat(encoder.matches(data.get("password"), createdUser.getPasswordDigest())).isTrue();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("id").isEqualTo(createdUser.getId());
            v.node("email").isEqualTo(createdUser.getEmail());
            v.node("firstName").isEqualTo(createdUser.getFirstName());
            v.node("lastName").isEqualTo(createdUser.getLastName());
            v.node("createdAt").isEqualTo(createdUser.getCreatedAt());
        });
    }

    @Test
    public void testCreateWithInvalidData() throws Exception {
        var data = Map.of(
            "email", "jack@google.com"
        );

        var request = post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        mockMvc.perform(request)
            .andExpect(status().isBadRequest());

        var createdUser = userRepository.findByEmail(data.get("email")).orElse(null);
        assertNull(createdUser);
    }

    @Test
    public void testUpdate() throws Exception {
        userRepository.save(user);

        var data = Map.of(
            "email", "jack@google.com",
            "firstName", "Jack",
            "lastName", "Jons",
            "password", "some-password"
        );

        var request = put("/api/users/" + user.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        var result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        var updatedUser = userRepository.findById(user.getId()).get();

        assertThat(data.get("email")).isEqualTo(updatedUser.getEmail());
        assertThat(data.get("firstName")).isEqualTo(updatedUser.getFirstName());
        assertThat(data.get("lastName")).isEqualTo(updatedUser.getLastName());
        assertThat(encoder.matches(data.get("password"), updatedUser.getPasswordDigest())).isTrue();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> {
            v.node("id").isEqualTo(updatedUser.getId());
            v.node("email").isEqualTo(updatedUser.getEmail());
            v.node("firstName").isEqualTo(updatedUser.getFirstName());
            v.node("lastName").isEqualTo(updatedUser.getLastName());
            v.node("createdAt").isEqualTo(updatedUser.getCreatedAt());
        });
    }

    @Test
    public void testUpdateWithInvalidData() throws Exception {
        userRepository.save(user);

        var data = Map.of(
            "email", ""
        );

        var request = put("/api/users/" + user.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        mockMvc.perform(request)
            .andExpect(status().isBadRequest());

        var updatedUser = userRepository.findById(user.getId()).get();
        assertThat(user.getEmail()).isEqualTo(updatedUser.getEmail());
    }

    @Test
    public void testUpdateWithEmptyData() throws Exception {
        userRepository.save(user);
        var data = Map.of();

        var request = put("/api/users/" + user.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        mockMvc.perform(request)
            .andExpect(status().isOk());

        var updatedUser = userRepository.findById(user.getId()).get();

        assertThat(user.getEmail()).isEqualTo(updatedUser.getEmail());
        assertThat(user.getFirstName()).isEqualTo(updatedUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(updatedUser.getLastName());
        assertThat(user.getPasswordDigest()).isEqualTo(updatedUser.getPasswordDigest());
    }

    @Test
    public void testUpdateOtherUser() throws Exception {
        var otherUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(otherUser);
        userRepository.save(user);

        var data = Map.of(
            "email", "jack@google.com",
            "firstName", "Jack",
            "lastName", "Jons",
            "password", "some-password"
        );

        var request = put("/api/users/" + otherUser.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(data))
            .with(token);

        mockMvc.perform(request)
            .andExpect(status().isForbidden());

            var updatedOtherUser = userRepository.findById(otherUser.getId()).get();
            assertThat(otherUser.getEmail()).isEqualTo(updatedOtherUser.getEmail());
    }

    @Test
    public void testDestroy() throws Exception {
        userRepository.save(user);
        var request = delete("/api/users/" + user.getId()).with(token);

        mockMvc.perform(request)
            .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(user.getId())).isFalse();
    }

    @Test
    public void testDestroyOtherUser() throws Exception {
        var otherUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(otherUser);
        userRepository.save(user);
        var request = delete("/api/users/" + otherUser.getId()).with(token);

        mockMvc.perform(request)
            .andExpect(status().isForbidden());

        assertThat(userRepository.existsById(otherUser.getId())).isTrue();
    }
}
