package cm.iusjc.userservice.controller;

import cm.iusjc.userservice.dto.RegisterRequest;
import cm.iusjc.userservice.dto.UserDTO;
import cm.iusjc.userservice.dto.ProfileUpdateRequest;
import cm.iusjc.userservice.dto.PasswordChangeRequest;
import cm.iusjc.userservice.dto.UserUpdateRequest;
import cm.iusjc.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }
    
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable String role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(userService.updateProfile(auth.getName(), request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        userService.changePassword(auth.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
