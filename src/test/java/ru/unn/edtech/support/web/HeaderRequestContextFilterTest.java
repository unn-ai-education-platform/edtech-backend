package ru.unn.edtech.support.web;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import ru.unn.edtech.support.RequestContext;
import ru.unn.edtech.support.UserRole;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderRequestContextFilterTest {

    private HeaderRequestContextFilter filter;
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().build();
        filter = new HeaderRequestContextFilter(jsonMapper);
    }

    @Test
    void publicEndpointsDoNotRequireUserHeaders() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/ping");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        RequestContext ctx = (RequestContext) request.getAttribute(HeaderRequestContextFilter.REQUEST_CONTEXT_ATTR);
        assertThat(ctx).isNotNull();
        assertThat(ctx.getUserId()).isNull();
        assertThat(ctx.getRole()).isNull();
        assertThat(ctx.getTraceId()).isNotBlank();
        assertThat(response.getHeader("X-Request-Id")).isEqualTo(ctx.getTraceId());
    }

    @Test
    void businessEndpointWithoutHeadersReturnsBadRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/teacher/ping");
        request.addHeader("X-Request-Id", "trace-400");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNull();
        assertThat(response.getStatus()).isEqualTo(400);
        JsonNode body = jsonMapper.readTree(response.getContentAsString());
        assertThat(body.get("code").asText()).isEqualTo("INVALID_REQUEST_HEADER");
        assertThat(body.get("traceId").asText()).isEqualTo("trace-400");
    }

    @Test
    void invalidRoleReturnsBadRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/teacher/ping");
        request.addHeader("X-User-Id", "user-1");
        request.addHeader("X-User-Role", "ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNull();
        assertThat(response.getStatus()).isEqualTo(400);
        JsonNode body = jsonMapper.readTree(response.getContentAsString());
        assertThat(body.get("code").asText()).isEqualTo("INVALID_REQUEST_HEADER");
    }

    @Test
    void requestIdIsPropagatedToContextAndResponse() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/teacher/ping");
        request.addHeader("X-User-Id", "teacher-1");
        request.addHeader("X-User-Role", "TEACHER");
        request.addHeader("X-Request-Id", "trace-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        RequestContext ctx = (RequestContext) request.getAttribute(HeaderRequestContextFilter.REQUEST_CONTEXT_ATTR);
        assertThat(chain.getRequest()).isNotNull();
        assertThat(ctx).isNotNull();
        assertThat(ctx.getRole()).isEqualTo(UserRole.TEACHER);
        assertThat(ctx.getTraceId()).isEqualTo("trace-123");
        assertThat(response.getHeader("X-Request-Id")).isEqualTo("trace-123");
    }

    @Test
    void requestIdIsGeneratedWhenHeaderMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/teacher/ping");
        request.addHeader("X-User-Id", "teacher-1");
        request.addHeader("X-User-Role", "TEACHER");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        RequestContext ctx = (RequestContext) request.getAttribute(HeaderRequestContextFilter.REQUEST_CONTEXT_ATTR);
        assertThat(chain.getRequest()).isNotNull();
        assertThat(ctx).isNotNull();
        assertThat(ctx.getTraceId()).isNotBlank();
        assertThat(response.getHeader("X-Request-Id")).isEqualTo(ctx.getTraceId());
    }
}
