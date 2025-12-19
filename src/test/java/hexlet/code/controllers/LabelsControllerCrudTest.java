package hexlet.code.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.labels.LabelCreateDto;
import hexlet.code.dto.labels.LabelUpdateDto;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.security.JwtService;
import hexlet.code.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LabelsControllerCrudTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;

    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private UserRepository userRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private LabelRepository labelRepository;
    @Autowired private TaskStatusRepository taskStatusRepository;

    private String bearer;

    @BeforeEach
    void setUp() {
        // порядок важен
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();

        var u = new User();
        u.setEmail("mvc@example.com");
        u.setPasswordHash(passwordEncoder.encode("pass"));
        u.setFirstName("M");
        u.setLastName("V");
        u = userRepository.save(u);

        bearer = "Bearer " + jwtService.issue(u.getId(), u.getEmail());
    }

    @Test
    void crud() throws Exception {
        var create = new LabelCreateDto();
        create.setName("backend");

        var createdJson = mockMvc.perform(post("/api/labels")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("backend"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long id = om.readTree(createdJson).get("id").asLong();

        mockMvc.perform(get("/api/labels/{id}", id)
                        .header("Authorization", bearer)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("backend"));

        mockMvc.perform(get("/api/labels")
                        .header("Authorization", bearer)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$", hasSize(1)));

        var upd = new LabelUpdateDto();
        upd.setName("frontend");

        mockMvc.perform(put("/api/labels/{id}", id)
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("frontend"));

        mockMvc.perform(delete("/api/labels/{id}", id)
                        .header("Authorization", bearer))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/labels/{id}", id)
                        .header("Authorization", bearer))
                .andExpect(status().isNotFound());
    }
}
