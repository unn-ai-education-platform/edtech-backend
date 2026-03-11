package ru.unn.edtech.support;

import ru.unn.edtech.support.exception.ForbiddenException;

public final class Access {

    private Access() {
        // utility class
    }

    public static void requireTeacher(RequestContext ctx) {
        if (ctx == null || ctx.getRole() != UserRole.TEACHER) {
            throw new ForbiddenException("Доступ разрешён только преподавателю");
        }
    }

    public static void requireStudent(RequestContext ctx) {
        if (ctx == null || ctx.getRole() != UserRole.STUDENT) {
            throw new ForbiddenException("Доступ разрешён только студенту");
        }
    }
}