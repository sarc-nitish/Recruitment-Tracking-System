package com.recruitcrm.rts.service;

import com.recruitcrm.rts.dto.RegisterRequest;
import com.recruitcrm.rts.dto.UserResponse;
import com.recruitcrm.rts.entity.Role;
import com.recruitcrm.rts.entity.User;
import com.recruitcrm.rts.exception.BadRequestException;
import com.recruitcrm.rts.exception.ConflictException;
import com.recruitcrm.rts.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registers users, and also tells Spring Security how to find a user at login time
 * (UserDetailsService).
 */
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Called by Spring Security on every request that sends an email + password. */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email " + email));
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("This email is already registered.");
        }

        boolean hasCompany = request.companyName() != null && !request.companyName().isBlank();
        if (request.role() == Role.RECRUITER && !hasCompany) {
            throw new BadRequestException("Company name is required for a RECRUITER.");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));   // BCrypt hash
        user.setRole(request.role());
        user.setCompanyName(request.role() == Role.RECRUITER ? request.companyName().trim() : null);
        return UserResponse.from(userRepository.save(user));
    }
}
