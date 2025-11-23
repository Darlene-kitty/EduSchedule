package cm.iusjc.userservice.service;

import cm.iusjc.userservice.config.JwtUtil;
import cm.iusjc.userservice.dto.LoginRequest;
import cm.iusjc.userservice.dto.LoginResponse;
import cm.iusjc.userservice.dto.RegisterRequest;
import cm.iusjc.userservice.dto.UserDTO;
import cm.iusjc.userservice.entity.RefreshToken;
import cm.iusjc.userservice.entity.User;
import cm.iusjc.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    public UserDTO register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        return userService.createUser(request);
    }
    
    private final RefreshTokenService refreshTokenService;
    
    public LoginResponse login(LoginRequest request) {
        try {
            // Authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            
            // Récupérer l'utilisateur
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Générer le token JWT
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            
            // Générer le refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
            
            log.info("User logged in successfully: {}", request.getUsername());
            
            return new LoginResponse(
                    token,
                    refreshToken.getToken(),
                    "Bearer",
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole()
            );
            
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", request.getUsername());
            throw new RuntimeException("Invalid username or password");
        }
    }
    
    public LoginResponse refreshToken(String refreshTokenStr) {
        return refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newToken = jwtUtil.generateToken(user.getUsername(), user.getRole());
                    log.info("Token refreshed for user: {}", user.getUsername());
                    
                    return new LoginResponse(
                            newToken,
                            refreshTokenStr,
                            "Bearer",
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getRole()
                    );
                })
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }
    
    public void revokeRefreshToken(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }
    
    public UserDTO getCurrentUser(String username) {
        return userService.getUserByUsername(username);
    }
}
