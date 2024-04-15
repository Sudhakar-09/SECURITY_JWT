package com.thbs.security.DTO;

import com.thbs.security.user.Role;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsDTO {
    private String firstName;

    private String lastName;

    @Email
    private String email;

    private Integer employeeId;

    private String businessUnit;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Constructor, getters, setters, etc.

}
