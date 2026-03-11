package ru.unn.edtech.support.web;

public record ApiErrorResponse(
        String code,
        String message,
        String traceId
) {}