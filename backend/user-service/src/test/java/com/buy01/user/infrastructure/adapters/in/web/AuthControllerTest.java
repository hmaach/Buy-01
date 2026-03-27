package com.buy01.user.infrastructure.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.buy01.user.domain.model.Role;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.in.AuthService;
import com.buy01.user.domain.port.in.AvatarService;
import com.buy01.user.domain.port.out.TokenGeneratorPort;
import com.buy01.user.domain.port.out.TokenResult;
import com.buy01.user.infrastructure.config.GlobalExceptionHandler;
import com.buy01.user.infrastructure.config.SecurityConfig;
import com.buy01.user.infrastructure.security.JwtAuthenticationFilter;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        GlobalExceptionHandler.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AvatarService avatarService;

    @MockitoBean
    private TokenGeneratorPort tokenGeneratorPort;

    @Test
    void registerReturnsCreatedUser() throws Exception {
        UUID userId = UUID.fromString("6a61d4a4-f740-45c5-8392-2d1e404d7b4e");
        User user = new User(
                userId,
                "Rachid",
                "rachid@example.com",
                "secret123",
                "https://cdn.buy01.com/avatars/u1.png",
                Role.SELLER,
                Instant.parse("2026-03-26T09:00:00Z"),
                Instant.parse("2026-03-26T09:00:00Z"));

        given(avatarService.saveAvatar(any(MultipartFile.class))).willReturn("https://cdn.buy01.com/avatars/u1.png");
        given(authService.register(any())).willReturn(user);

        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "avatar.png",
                "image/png",
                "avatar-bytes".getBytes());

        mockMvc.perform(multipart("/users/auth/register")
                        .file(avatar)
                        .param("name", "Rachid")
                        .param("email", "rachid@example.com")
                        .param("password", "secret123")
                        .param("role", "SELLER"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Rachid"))
                .andExpect(jsonPath("$.email").value("rachid@example.com"))
                .andExpect(jsonPath("$.avatarUrl").value("https://cdn.buy01.com/avatars/u1.png"))
                .andExpect(jsonPath("$.role").value("SELLER"));

        verify(avatarService).saveAvatar(any(MultipartFile.class));
        verify(authService).register(any());
    }

    @Test
    void loginReturnsTokenPayload() throws Exception {
        given(authService.login(any())).willReturn(new TokenResult(
                "jwt-token",
                Instant.parse("2026-03-27T09:00:00Z")));

        mockMvc.perform(post("/users/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "rachid@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.expiresAt").value("2026-03-27T09:00:00Z"));
    }

    @Test
    void loginReturnsBadRequestWhenPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/users/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "bad-email",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Validation failed")));
    }
}
