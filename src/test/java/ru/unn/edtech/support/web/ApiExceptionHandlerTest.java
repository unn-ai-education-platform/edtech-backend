package ru.unn.edtech.support.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import ru.unn.edtech.support.RequestContext;
import ru.unn.edtech.support.UserRole;
import ru.unn.edtech.support.exception.ApiException;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void apiExceptionUsesStatusCodeAndTraceIdFromRequestContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(
                HeaderRequestContextFilter.REQUEST_CONTEXT_ATTR,
                new RequestContext("teacher-1", UserRole.TEACHER, "ctx-trace")
        );
        ApiException ex = new ApiException(HttpStatus.BAD_REQUEST, "INVALID", "bad request");

        ResponseEntity<ApiErrorResponse> response = handler.handleApiException(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID");
        assertThat(response.getBody().message()).isEqualTo("bad request");
        assertThat(response.getBody().traceId()).isEqualTo("ctx-trace");
    }

    @Test
    void unexpectedExceptionUsesRequestIdHeaderWhenContextMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "hdr-trace");

        ResponseEntity<ApiErrorResponse> response = handler.handleUnexpectedException(request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().traceId()).isEqualTo("hdr-trace");
    }

    @Test
    void unexpectedExceptionUsesUnknownWhenNoContextAndNoRequestId() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<ApiErrorResponse> response = handler.handleUnexpectedException(request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().traceId()).isEqualTo("unknown");
    }
}
