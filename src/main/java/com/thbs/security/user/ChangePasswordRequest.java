package com.thbs.security.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// Lombok annotations to automatically generate getter, setter, and builder pattern methods.
@Getter
@Setter
@Builder
public class ChangePasswordRequest {

    // Fields representing the current password, new password, and password confirmation.
    private String currentPassword; // Current password of the user.
    private String newPassword; // New password that the user wants to set.
    private String confirmationPassword; // Confirmation of the new password.

}
