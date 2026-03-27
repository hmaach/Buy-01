package com.buy01.user.infrastructure.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.buy01.user.domain.model.Role;
import com.buy01.user.domain.model.User;
import com.buy01.user.domain.port.in.AvatarService;
import com.buy01.user.domain.port.in.UserService;
import com.buy01.user.domain.port.out.TokenGeneratorPort;
import com.buy01.user.infrastructure.config.GlobalExceptionHandler;
import com.buy01.user.infrastructure.config.SecurityConfig;
import com.buy01.user.infrastructure.security.JwtAuthenticationFilter;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        GlobalExceptionHandler.class
})
class UserControllerTest {

    private static final String TOKEN = "user-token";
    private static final UUID USER_ID = UUID.fromString("3f923efd-2f6c-47d2-a9e3-644ab31f9314");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AvatarService avatarService;

    @MockitoBean
    private TokenGeneratorPort tokenGeneratorPort;

    @Test
    void getCurrentUserReturnsAuthenticatedUser() throws Exception {
        givenAuthenticatedUser();
        given(userService.findById(USER_ID))
                .willReturn(user("Rachid", "rachid@example.com", "https://cdn.buy01.com/avatars/u1.png"));

        mockMvc.perform(get("/users/me")
                .header(HttpHeaders.AUTHORIZATION, bearer(TOKEN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.name").value("Rachid"))
                .andExpect(jsonPath("$.email").value("rachid@example.com"))
                .andExpect(jsonPath("$.role").value("SELLER"));
    }

    @Test
    void checkUserExistsReturnsBoolean() throws Exception {
        givenAuthenticatedUser();
        given(userService.existsById(USER_ID)).willReturn(true);

        mockMvc.perform(get("/users/exists/{id}", USER_ID)
                .header(HttpHeaders.AUTHORIZATION, bearer(TOKEN)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void updateCurrentUserReturnsUpdatedUser() throws Exception {
        givenAuthenticatedUser();
        given(avatarService.saveAvatar(any(MultipartFile.class))).willReturn("https://cdn.buy01.com/avatars/u2.png");
        given(userService.updateUser(eq(USER_ID), any()))
                .willReturn(user("Rachid Updated", "updated@example.com", "https://cdn.buy01.com/avatars/u2.png"));

        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "avatar.png",
                "image/png",
                "avatar-bytes".getBytes());

        mockMvc.perform(multipart("/users/me")
                .file(avatar)
                .param("name", "Rachid Updated")
                .param("email", "updated@example.com")
                        .with(request -> {
                            request.setMethod("PUT");
                            request.addHeader(HttpHeaders.AUTHORIZATION, bearer(TOKEN));
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.name").value("Rachid Updated"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.avatarUrl").value("https://cdn.buy01.com/avatars/u2.png"));

        verify(avatarService).saveAvatar(any(MultipartFile.class));
        verify(userService).updateUser(eq(USER_ID), any());
    }

    @Test
    void deleteCurrentUserDeletesAuthenticatedUser() throws Exception {
        givenAuthenticatedUser();
        given(userService.findById(USER_ID))
                .willReturn(user("Rachid", "rachid@example.com", "https://cdn.buy01.com/avatars/u1.png"));

        mockMvc.perform(delete("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(TOKEN)))
                .andExpect(status().isOk());

        verify(userService).deleteUser(USER_ID);
    }

    private void givenAuthenticatedUser() {
        given(tokenGeneratorPort.validateToken(TOKEN)).willReturn(true);
        given(tokenGeneratorPort.extractEmail(TOKEN)).willReturn("rachid@example.com");
        given(tokenGeneratorPort.extractUserId(TOKEN)).willReturn(USER_ID);
        given(tokenGeneratorPort.extractRole(TOKEN)).willReturn(Role.SELLER);
    }

    private User user(String name, String email, String avatarUrl) {
        return new User(
                USER_ID,
                name,
                email,
                "secret123",
                avatarUrl,
                Role.SELLER,
                Instant.parse("2026-03-26T08:00:00Z"),
                Instant.parse("2026-03-26T08:00:00Z"));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
