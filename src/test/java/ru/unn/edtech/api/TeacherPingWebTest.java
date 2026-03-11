package ru.unn.edtech.api;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.unn.edtech.support.web.ApiExceptionHandler;
import ru.unn.edtech.support.web.HeaderRequestContextFilter;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeacherPingWebTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new PingController())
            .setControllerAdvice(new ApiExceptionHandler())
            .addFilters(new HeaderRequestContextFilter(JsonMapper.builder().build()))
            .build();

    @Test
    void teacherPingReturnsOkForTeacher() throws Exception {
        mockMvc.perform(get("/api/v1/teacher/ping")
                        .header("X-User-Id", "teacher-1")
                        .header("X-User-Role", "TEACHER")
                        .header("X-Request-Id", "req-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(header().string("X-Request-Id", "req-1"));
    }

    @Test
    void teacherPingReturnsForbiddenForStudent() throws Exception {
        mockMvc.perform(get("/api/v1/teacher/ping")
                        .header("X-User-Id", "student-1")
                        .header("X-User-Role", "STUDENT")
                        .header("X-Request-Id", "trace-student"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.traceId").value("trace-student"));
    }

    @Test
    void teacherPingReturnsBadRequestWhenRequiredHeadersMissing() throws Exception {
        mockMvc.perform(get("/api/v1/teacher/ping")
                        .header("X-Request-Id", "trace-missing"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_HEADER"))
                .andExpect(jsonPath("$.traceId").value("trace-missing"));
    }
}
