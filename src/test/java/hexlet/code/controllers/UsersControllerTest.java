package hexlet.code.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.users.UserCreateDto;
import hexlet.code.dto.users.UserResponseDto;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.mapper.UserMapper;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("mvc+" + System.nanoTime() + "@example.com");
        testUser.setPasswordHash("passwordHash");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("GET /api/users возвращает тот же список, что и в БД")
    void listUsersMatchesDatabase() throws Exception {
        MvcResult resp = mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = resp.getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<UserResponseDto> fromApi = objectMapper.readValue(json, new TypeReference<List<UserResponseDto>>() { });

        List<User> fromDb = userRepository.findAll();
        List<UserResponseDto> fromDbDtos = fromDb.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        assertThat(fromApi)
                .containsExactlyInAnyOrderElementsOf(fromDbDtos);

        assertThat(fromApi).hasSize(fromDb.size());
    }

    @Test
    public void testCreate() throws Exception {
        var data = new UserCreateDto(
                "newuser@example.com",
                "John",
                "Doe",
                "password123"
        );

        var request = post("/api/users")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data));

        mockMvc.perform(request).andExpect(status().isCreated());

        var user = userRepository.findByEmail(data.email()).orElse(null);

        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(data.firstName());
        assertThat(user.getLastName()).isEqualTo(data.lastName());
    }

    @Test
    public void testIndex() throws Exception {
        var response = mockMvc.perform(get("/api/users").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        String body = response.getResponse().getContentAsString();

        List<UserResponseDto> userDTOS = objectMapper.readValue(body, new TypeReference<List<UserResponseDto>>() { });

        var expected = userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        assertThat(userDTOS).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new HashMap<String, Object>();
        data.put("firstName", "Updated Name");

        var request = put("/api/users/" + testUser.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data));

        mockMvc.perform(request).andExpect(status().isOk());

        var user = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(user.getFirstName()).isEqualTo("Updated Name");
    }

    @Test
    public void testDelete() throws Exception {
        var request = delete("/api/users/" + testUser.getId())
                .with(jwt());

        mockMvc.perform(request).andExpect(status().isNoContent());

        var user = userRepository.findById(testUser.getId());
        assertThat(user).isEmpty();
    }

    @Test
    public void testShow() throws Exception {
        var request = get("/api/users/" + testUser.getId())
                .with(jwt());

        var result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(v -> v.node("email").isEqualTo(testUser.getEmail()),
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()));
    }

}
