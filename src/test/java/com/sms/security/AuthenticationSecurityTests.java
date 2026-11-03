package com.sms.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Public can access the login page")
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("Unauthenticated request to Teacher Portal redirects to /login")
    void unauthenticatedTeacherAccessRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/teacher/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Unauthenticated request to Student Portal redirects to /login")
    void unauthenticatedStudentAccessRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Teacher login with valid credentials redirects to Teacher Dashboard")
    void teacherLoginSuccess() throws Exception {
        mockMvc.perform(formLogin("/login").user("teacher").password("password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher/dashboard"));
    }

    @Test
    @DisplayName("Student login with valid credentials redirects to Student Dashboard")
    void studentLoginSuccess() throws Exception {
        mockMvc.perform(formLogin("/login").user("student").password("password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/dashboard"));
    }

    @Test
    @DisplayName("Login with invalid password triggers redirect with error parameter")
    void invalidPasswordFails() throws Exception {
        mockMvc.perform(formLogin("/login").user("teacher").password("wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @WithMockUser(username = "student", authorities = {"ROLE_STUDENT"})
    @DisplayName("Student attempting to access Teacher Portal receives HTTP 403 Forbidden")
    void studentCannotAccessTeacherPortal() throws Exception {
        mockMvc.perform(get("/teacher/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher", authorities = {"ROLE_TEACHER"})
    @DisplayName("Teacher attempting to access Student Portal receives HTTP 403 Forbidden")
    void teacherCannotAccessStudentPortal() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher", authorities = {"ROLE_TEACHER"})
    @DisplayName("Authenticated teacher can access Teacher Dashboard with 200 OK")
    void teacherAccessTeacherDashboardAllowed() throws Exception {
        mockMvc.perform(get("/teacher/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("teacher/dashboard"));
    }

    @Test
    @WithMockUser(username = "student", authorities = {"ROLE_STUDENT"})
    @DisplayName("Authenticated student can access Student Dashboard with 200 OK")
    void studentAccessStudentDashboardAllowed() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/dashboard"));
    }

    @Test
    @WithMockUser(username = "teacher", authorities = {"ROLE_TEACHER"})
    @DisplayName("Logout invalidates session and redirects to /login?logout=true")
    void logoutSucceeds() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }
}

