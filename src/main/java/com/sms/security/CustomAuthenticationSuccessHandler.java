package com.sms.security;

import com.sms.entity.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (Role.ROLE_TEACHER.name().equals(role) || Role.ROLE_ADMIN.name().equals(role)) {
                response.sendRedirect(request.getContextPath() + "/teacher/dashboard");
                return;
            } else if (Role.ROLE_STUDENT.name().equals(role)) {
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
                return;
            }
        }
        response.sendRedirect(request.getContextPath() + "/login?error=true");
    }
}

