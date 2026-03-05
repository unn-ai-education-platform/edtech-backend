package ru.unn.edtech.support.web;

import tools.jackson.databind.json.JsonMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.unn.edtech.support.RequestContext;
import ru.unn.edtech.support.UserRole;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HeaderRequestContextFilter extends OncePerRequestFilter {

    public static final String REQUEST_CONTEXT_ATTR = "REQUEST_CONTEXT";

    private static final String H_USER_ID = "X-User-Id";
    private static final String H_USER_ROLE = "X-User-Role";
    private static final String H_REQUEST_ID = "X-Request-Id";

    private static final int MAX_HEADER_LEN = 128;

    private final JsonMapper jsonMapper;

    public HeaderRequestContextFilter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // traceId берем из X-Request-Id или генерируем
        String traceId = resolveTraceId(request);

        response.setHeader(H_REQUEST_ID, traceId);

        // Исключение: публичные эндпоинты проверки health/liveness допускаются без заголовков userId/role
        if (isPublicEndpoint(request)) {
            request.setAttribute(REQUEST_CONTEXT_ATTR, new RequestContext(null, null, traceId));
            filterChain.doFilter(request, response);
            return;
        }

        String userIdRaw = request.getHeader(H_USER_ID);
        String roleRaw = request.getHeader(H_USER_ROLE);

        // валидируем userId
        if (!StringUtils.hasText(userIdRaw)) {
            writeBadRequest(response, traceId, "INVALID_REQUEST_HEADER",
                    "Header отсутствует или пустой: " + H_USER_ID);
            return;
        }
        String userId = userIdRaw.trim();
        if (userId.length() > MAX_HEADER_LEN) {
            writeBadRequest(response, traceId, "INVALID_REQUEST_HEADER",
                    "Header слишком длинный: " + H_USER_ID);
            return;
        }

        // валидируем/парсим role
        if (!StringUtils.hasText(roleRaw)) {
            writeBadRequest(response, traceId, "INVALID_REQUEST_HEADER",
                    "Header отсутствует или пустой: " + H_USER_ROLE);
            return;
        }
        String roleNormalized = roleRaw.trim();
        if (roleNormalized.length() > MAX_HEADER_LEN) {
            writeBadRequest(response, traceId, "INVALID_REQUEST_HEADER",
                    "Header слишком длинный: " + H_USER_ROLE);
            return;
        }

        UserRole role;
        try {
            role = UserRole.valueOf(roleNormalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            writeBadRequest(response, traceId, "INVALID_REQUEST_HEADER",
                    "Недопустимое значение header: " + H_USER_ROLE);
            return;
        }

        // создаем контекст и кладем в request attributes
        RequestContext ctx = new RequestContext(userId, role, traceId);
        request.setAttribute(REQUEST_CONTEXT_ATTR, ctx);

        // пропускаем дальше
        filterChain.doFilter(request, response);
    }

    private String resolveTraceId(HttpServletRequest request) {
        String incoming = request.getHeader(H_REQUEST_ID);
        if (StringUtils.hasText(incoming)) {
            String trimmed = incoming.trim();
            if (trimmed.length() <= MAX_HEADER_LEN) {
                return trimmed;
            }
            // слишком длинный — не принимаем, генерим безопасный
        }
        return UUID.randomUUID().toString();
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Базовый health endpoint
        if ("/api/v1/ping".equals(uri)) {
            return true;
        }
        // Spring Boot actuator health/info
        if (uri != null && uri.startsWith("/actuator/health")) {
            return true;
        }
        return "/actuator/info".equals(uri);
    }

    private void writeBadRequest(HttpServletResponse response,
                                 String traceId,
                                 String code,
                                 String message) throws IOException {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // формат ошибок
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("traceId", traceId);

        jsonMapper.writeValue(response.getWriter(), body);
    }
}