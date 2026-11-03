package com.sms.security;

import com.sms.entity.Role;
import com.sms.entity.StudentProfile;
import com.sms.entity.TeacherProfile;
import com.sms.entity.User;
import com.sms.repository.StudentProfileRepository;
import com.sms.repository.TeacherProfileRepository;
import com.sms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentProfileRepository studentProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

        String displayName = user.getUsername();
        if (user.getRole() == Role.ROLE_TEACHER) {
            displayName = teacherProfileRepository.findByUserId(user.getId())
                    .map(TeacherProfile::getFullName)
                    .orElse(user.getUsername());
        } else if (user.getRole() == Role.ROLE_STUDENT) {
            displayName = studentProfileRepository.findByUserId(user.getId())
                    .map(StudentProfile::getFullName)
                    .orElse(user.getUsername());
        }

        return new CustomUserDetails(user, displayName);
    }
}

