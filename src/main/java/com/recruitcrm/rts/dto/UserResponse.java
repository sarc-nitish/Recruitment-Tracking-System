package com.recruitcrm.rts.dto;

import com.recruitcrm.rts.entity.Role;
import com.recruitcrm.rts.entity.User;

/** What we send back about a user. The password is never included. */
public record UserResponse(Long id, String name, String email, Role role, String companyName) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getRole(), user.getCompanyName());
    }
}
