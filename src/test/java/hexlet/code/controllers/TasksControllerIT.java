package hexlet.code.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TasksControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private UserRepository userRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private TaskStatusRepository taskStatusRepository;

    private String bearer;
    private TaskStatus statusNew;
    private TaskStatus statusDone;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();

        var u = new User();
        u.setEmail("mvc@example.com");
        u.setPasswordHash(passwordEncoder.encode("pass"));
        u.setFirstName("M");
        u.setLastName("V");
        u = userRepository.save(u);

        bearer = "Bearer " + jwtService.issue(u.getId(), u.getEmail());

        statusNew = new TaskStatus();
        statusNew.setName("New");
        statusNew.setSlug("new");
        statusNew = taskStatusRepository.save(statusNew);

        statusDone = new TaskStatus();
        statusDone.setName("Done");
        statusDone.setSlug("done");
        statusDone = taskStatusRepository.save(statusDone);
    }

    @Test
    void crud() throws Exception {
        var createdJson = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Task #1",
                                "content", "Hello",
                                "status", "new"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Task #1"))
                .andExpect(jsonPath("$.content").value("Hello"))
                .andExpect(jsonPath("$.status").value("new"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long id = objectMapper.readTree(createdJson).get("id").asLong();

        mockMvc.perform(get("/api/tasks/{id}", id)
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Task #1"))
                .andExpect(jsonPath("$.status").value("new"));

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));

        mockMvc.perform(put("/api/tasks/{id}", id)
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Task #1 updated",
                                "content", "Updated text",
                                "status", "done"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Task #1 updated"))
                .andExpect(jsonPath("$.content").value("Updated text"))
                .andExpect(jsonPath("$.status").value("done"));

        mockMvc.perform(delete("/api/tasks/{id}", id)
                        .header("Authorization", bearer))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}", id)
                        .header("Authorization", bearer))
                .andExpect(status().isNotFound());
    }

    @Test
    void listSupportsPaginationHeaders() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "A",
                                "status", "new"
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "B",
                                "status", "new"
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", bearer)
                        .param("_start", "0")
                        .param("_end", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "2"))
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
