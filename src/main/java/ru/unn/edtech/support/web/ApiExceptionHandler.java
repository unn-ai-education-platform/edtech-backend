package ru.unn.edtech.support.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.unn.edtech.support.RequestContext;
import ru.unn.edtech.support.exception.ApiException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final String H_REQUEST_ID = "X-Request-Id";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        String traceId = resolveTraceId(request);

        ApiErrorResponse body = new ApiErrorResponse(
                ex.getCode(),
                ex.getMessage(),
                traceId
        );

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(HttpServletRequest request) {
        String traceId = resolveTraceId(request);

        ApiErrorResponse body = new ApiErrorResponse(
                "INTERNAL_ERROR",
                "Внутренняя ошибка сервера",
                traceId
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }

    private String resolveTraceId(HttpServletRequest request) {
        Object ctxAttr = request.getAttribute(HeaderRequestContextFilter.REQUEST_CONTEXT_ATTR);

        if (ctxAttr instanceof RequestContext ctx) {
            return ctx.getTraceId();
        }

        String requestIdHeader = request.getHeader(H_REQUEST_ID);
        if (StringUtils.hasText(requestIdHeader)) {
            return requestIdHeader.trim();
        }

        return "unknown";
    }
}