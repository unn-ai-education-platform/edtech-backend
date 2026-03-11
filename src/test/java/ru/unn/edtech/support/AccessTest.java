package ru.unn.edtech.support;

import org.junit.jupiter.api.Test;
import ru.unn.edtech.support.exception.ForbiddenException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccessTest {

    @Test
    void requireTeacherAllowsTeacherOnly() {
        RequestContext teacherCtx = new RequestContext("teacher-1", UserRole.TEACHER, "t-1");
        RequestContext studentCtx = new RequestContext("student-1", UserRole.STUDENT, "t-2");

        assertThatCode(() -> Access.requireTeacher(teacherCtx)).doesNotThrowAnyException();
        assertThatThrownBy(() -> Access.requireTeacher(studentCtx)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> Access.requireTeacher(null)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void requireStudentAllowsStudentOnly() {
        RequestContext teacherCtx = new RequestContext("teacher-1", UserRole.TEACHER, "t-1");
        RequestContext studentCtx = new RequestContext("student-1", UserRole.STUDENT, "t-2");

        assertThatCode(() -> Access.requireStudent(studentCtx)).doesNotThrowAnyException();
        assertThatThrownBy(() -> Access.requireStudent(teacherCtx)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> Access.requireStudent(null)).isInstanceOf(ForbiddenException.class);
    }
}
