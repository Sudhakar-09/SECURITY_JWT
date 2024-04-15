package com.thbs.security.user;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thbs.security.DTO.UserDetailsDTO;

// import com.thbs.security.Dto.UserDto;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/users") // Base URL path for the UserController // Lombok annotation to generate a constructor with required arguments
@CrossOrigin(origins = {"172.18.4.192", "172.18.5.13"})
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserService service; // Instance of UserService injected via constructor

    @Autowired
    private UserRepository userRepository;
    
    // Endpoint to handle PATCH requests for changing user passwords
    @PatchMapping
    public ResponseEntity<?> changePassword(
          @RequestBody ChangePasswordRequest request, // Request body containing password change information
          Principal connectedUser // Principal object representing the currently authenticated user
) {
        service.changePassword(request, connectedUser); // Delegate password change operation to UserService
        return ResponseEntity.ok().build(); // Return a response indicating success (HTTP 200 OK)
    
}
    @GetMapping("/userdetails")
    public ResponseEntity<List<UserDetailsDTO>> getFilteredUsers() {
        List<User> users = userRepository.findAllByRole(Role.USER);
        List<UserDetailsDTO> userDTOs = users.stream()
            .map(user -> new UserDetailsDTO(
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getEmployeeId(),
                user.getBusinessUnit(),
                user.getRole()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }
}
    
