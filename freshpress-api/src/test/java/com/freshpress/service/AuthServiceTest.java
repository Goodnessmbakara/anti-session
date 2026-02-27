package com.freshpress.service;

import com.freshpress.dto.AuthResponse;
import com.freshpress.dto.LoginRequest;
import com.freshpress.dto.RegisterRequest;
import com.freshpress.model.Role;
import com.freshpress.model.User;
import com.freshpress.repository.UserRepository;
import com.freshpress.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Jane Doe");
        registerRequest.setEmail("jane@freshpress.com");
        registerRequest.setPassword("secret123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("jane@freshpress.com");
        loginRequest.setPassword("secret123");

        savedUser = User.builder()
                .fullName("Jane Doe")
                .email("jane@freshpress.com")
                .password("$encoded$")
                .role(Role.ADMIN)
                .build();
    }

    // ─── REGISTER ────────────────────────────────────────────────

    @Test
    @DisplayName("register() - should return AuthResponse with token when email is new")
    void register_withNewEmail_returnsAuthResponse() {
        when(userRepository.existsByEmail("jane@freshpress.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$encoded$");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getEmail()).isEqualTo("jane@freshpress.com");
        assertThat(response.getFullName()).isEqualTo("Jane Doe");
        assertThat(response.getRole()).isEqualTo("ADMIN");

        verify(userRepository).existsByEmail("jane@freshpress.com");
        verify(passwordEncoder).encode("secret123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    @DisplayName("register() - should throw RuntimeException when email is already registered")
    void register_withDuplicateEmail_throwsRuntimeException() {
        when(userRepository.existsByEmail("jane@freshpress.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("jane@freshpress.com");

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("register() - should hash the password before saving")
    void register_shouldEncodePasswordBeforeSave() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$hashed$");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("token");

        authService.register(registerRequest);

        verify(passwordEncoder).encode("secret123");
        // Captures argument to verify plaintext is never saved
        verify(userRepository).save(argThat(u -> "$hashed$".equals(u.getPassword())));
    }

    // ─── LOGIN ────────────────────────────────────────────────────

    @Test
    @DisplayName("login() - should authenticate and return AuthResponse with token")
    void login_withValidCredentials_returnsAuthResponse() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("jane@freshpress.com")).thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(savedUser)).thenReturn("login-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("login-jwt-token");
        assertThat(response.getEmail()).isEqualTo("jane@freshpress.com");
        assertThat(response.getFullName()).isEqualTo("Jane Doe");
        assertThat(response.getRole()).isEqualTo("ADMIN");

        verify(authenticationManager).authenticate(
                argThat(auth -> auth instanceof UsernamePasswordAuthenticationToken
                        && "jane@freshpress.com".equals(auth.getPrincipal())));
    }

    @Test
    @DisplayName("login() - should propagate exception from AuthenticationManager on bad credentials")
    void login_withWrongPassword_throwsBadCredentialsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("login() - should throw RuntimeException when user not found after auth (edge case)")
    void login_whenUserDisappearsAfterAuth_throwsRuntimeException() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("jane@freshpress.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }
}
