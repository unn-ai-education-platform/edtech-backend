package ru.unn.edtech.api;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.unn.edtech.support.web.HeaderRequestContextFilter;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

class PingControllerWebTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new PingController())
            .addFilters(new HeaderRequestContextFilter(JsonMapper.builder().build()))
            .build();

    @Test
    void pingIsPublicAndReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"))
                .andExpect(header().exists("X-Request-Id"));
    }
}
