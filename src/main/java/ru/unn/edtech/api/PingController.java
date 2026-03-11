package ru.unn.edtech.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.unn.edtech.support.Access;
import ru.unn.edtech.support.RequestContext;
import ru.unn.edtech.support.web.HeaderRequestContextFilter;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PingController {

    @GetMapping("/ping")
    public String ping() {
        return "ok";
    }

    @GetMapping("/teacher/ping")
    public Map<String, String> teacherPing(HttpServletRequest request) {
        RequestContext ctx = (RequestContext) request.getAttribute(HeaderRequestContextFilter.REQUEST_CONTEXT_ATTR);

        Access.requireTeacher(ctx);

        return Map.of("status", "ok");
    }
}