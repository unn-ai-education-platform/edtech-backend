package ru.unn.edtech.support;

import lombok.Value;

/**
 * Единый объект, который будут использовать сервисы/контроллеры для access-checks и логов.
 */
@Value
public class RequestContext {
    String userId;
    UserRole role;
    String traceId;
}
