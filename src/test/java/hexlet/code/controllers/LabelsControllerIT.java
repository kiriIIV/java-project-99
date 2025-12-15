package hexlet.code.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.labels.LabelCreateDto;
import hexlet.code.dto.labels.LabelDto;
import hexlet.code.dto.labels.LabelUpdateDto;
import hexlet.code.repository.LabelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class LabelsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LabelRepository labelRepository;

    @BeforeEach
    void setUp() {
        labelRepository.deleteAll();
    }

    @Test
    void createAndGetById() throws Exception {
        LabelCreateDto create = new LabelCreateDto();
        create.setName("bug");

        var createResp = mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andReturn();

        LabelDto created = objectMapper.readValue(
                createResp.getResponse().getContentAsString(StandardCharsets.UTF_8),
                LabelDto.class
        );

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("bug");

        var getResp = mockMvc.perform(get("/api/labels/{id}", created.getId()))
                .andExpect(status().isOk())
                .andReturn();

        LabelDto fromApi = objectMapper.readValue(
                getResp.getResponse().getContentAsString(StandardCharsets.UTF_8),
                LabelDto.class
        );

        assertThat(fromApi.getId()).isEqualTo(created.getId());
        assertThat(fromApi.getName()).isEqualTo(created.getName());
    }

    @Test
    void listWithTotalCountAndPagination() throws Exception {
        createLabel("aab");
        createLabel("bbc");
        createLabel("ccd");

        var allResp = mockMvc.perform(get("/api/labels"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "3"))
                .andReturn();

        List<LabelDto> all = objectMapper.readValue(
                allResp.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() { }
        );

        assertThat(all).hasSize(3);

        var pageResp = mockMvc.perform(get("/api/labels")
                        .param("_start", "0")
                        .param("_end", "2"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "3"))
                .andReturn();

        List<LabelDto> page = objectMapper.readValue(
                pageResp.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() { }
        );

        assertThat(page).hasSize(2);
    }

    @Test
    void updateAndDelete() throws Exception {
        Long id = createLabel("old");

        LabelUpdateDto update = new LabelUpdateDto();
        update.setName("new");

        mockMvc.perform(put("/api/labels/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());

        var getResp = mockMvc.perform(get("/api/labels/{id}", id))
                .andExpect(status().isOk())
                .andReturn();

        LabelDto updated = objectMapper.readValue(
                getResp.getResponse().getContentAsString(StandardCharsets.UTF_8),
                LabelDto.class
        );

        assertThat(updated.getName()).isEqualTo("new");

        mockMvc.perform(delete("/api/labels/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/labels/{id}", id))
                .andExpect(status().isNotFound());
    }

    private Long createLabel(String name) throws Exception {
        LabelCreateDto dto = new LabelCreateDto();
        dto.setName(name);

        var resp = mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        LabelDto created = objectMapper.readValue(
                resp.getResponse().getContentAsString(StandardCharsets.UTF_8),
                LabelDto.class
        );
        return created.getId();
    }
}
