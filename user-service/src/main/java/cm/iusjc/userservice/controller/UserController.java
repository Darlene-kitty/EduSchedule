package cm.iusjc.userservice.controller;

import cm.iusjc.userservice.dto.RegisterRequest;
import cm.iusjc.userservice.dto.UserDTO;
import cm.iusjc.userservice.dto.ProfileUpdateRequest;
import cm.iusjc.userservice.dto.PasswordChangeRequest;
import cm.iusjc.userservice.dto.UserUpdateRequest;
import cm.iusjc.userservice.dto.TeacherSchoolAssignmentDTO;
import cm.iusjc.userservice.service.UserService;
import cm.iusjc.userservice.service.TeacherSchoolAssignmentService;
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
    private final TeacherSchoolAssignmentService assignmentService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
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
        return ResponseEntity.ok(userService.updateUser(id, request));
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

    /**
     * Permuter un utilisateur vers une nouvelle école (école principale)
     */
    @PutMapping("/{id}/switch-school/{schoolId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> switchSchool(@PathVariable Long id, @PathVariable Long schoolId) {
        assignmentService.setPrimarySchool(id, schoolId);
        return ResponseEntity.ok().build();
    }

    /**
     * Assigner un utilisateur à une école
     */
    @PostMapping("/{id}/assign-school")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherSchoolAssignmentDTO> assignSchool(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> body) {
        TeacherSchoolAssignmentDTO dto = new TeacherSchoolAssignmentDTO();
        dto.setTeacherId(id);
        dto.setSchoolId(Long.valueOf(body.get("schoolId").toString()));
        dto.setSchoolName(body.getOrDefault("schoolName", "").toString());
        dto.setEffectiveFrom(java.time.LocalDateTime.now());
        dto.setIsActive(true);
        dto.setIsPrimarySchool(Boolean.parseBoolean(body.getOrDefault("isPrimary", "false").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.createAssignment(dto));
    }

    /**
     * Récupérer les écoles d'un utilisateur
     */
    @GetMapping("/{id}/schools")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<java.util.List<TeacherSchoolAssignmentDTO>> getUserSchools(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getTeacherSchools(id));
    }
}
