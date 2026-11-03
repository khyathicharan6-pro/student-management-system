package com.sms.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Authentication authentication,
                            Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isTeacher = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER") || a.getAuthority().equals("ROLE_ADMIN"));
            if (isTeacher) {
                return "redirect:/teacher/dashboard";
            } else {
                return "redirect:/student/dashboard";
            }
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password. Please verify your academic credentials.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been securely logged out of the portal.");
        }
        return "auth/login";
    }

    @GetMapping("/")
    public String rootRedirect(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isTeacher = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER") || a.getAuthority().equals("ROLE_ADMIN"));
            return isTeacher ? "redirect:/teacher/dashboard" : "redirect:/student/dashboard";
        }
        return "redirect:/login";
    }
}

