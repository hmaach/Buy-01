package com.buy01.media.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.buy01.media.domain.model.FileStatus;
import com.buy01.media.domain.model.Media;
import com.buy01.media.domain.ports.inbound.MediaUseCase;
import com.buy01.media.infrastructure.config.SecurityConfig;
import com.buy01.media.infrastructure.security.JwtAuthenticationFilter;
import com.buy01.media.infrastructure.security.JwtUtil;
import com.buy01.media.infrastructure.web.exception.GlobalExceptionHandler;

@WebMvcTest(MediaController.class)
@ActiveProfiles("test")
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        GlobalExceptionHandler.class
})
class MediaControllerTest {

    private static final String TOKEN = "media-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaUseCase mediaUseCase;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void testEndpointReturnsServiceStatus() throws Exception {
        mockMvc.perform(get("/media"))
                .andExpect(status().isOk())
                .andExpect(content().string("media service is working"));
    }

    @Test
    void getImageReturnsResourceWithHeaders() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("image-bytes".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "preview.png";
            }
        };

        given(mediaUseCase.getImageFile("img-1")).willReturn(resource);
        given(mediaUseCase.detectContentType(resource)).willReturn(MediaType.IMAGE_PNG);

        mockMvc.perform(get("/media/img-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"preview.png\""))
                .andExpect(content().bytes("image-bytes".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void getImageBatchReturnsMappedUrls() throws Exception {
        given(mediaUseCase.findImageUrlsByProductIds(eq(Set.of("prod-1", "prod-2"))))
                .willReturn(Map.of("prod-1", "/media/img-1", "prod-2", "/media/img-2"));

                mockMvc.perform(post("/media/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productIds": ["prod-1", "prod-2"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['prod-1']").value("/media/img-1"))
                .andExpect(jsonPath("$['prod-2']").value("/media/img-2"));
    }

    @Test
    void getProductImagesReturnsImageIds() throws Exception {
        given(mediaUseCase.getProdutImages("prod-77")).willReturn(List.of("img-10", "img-11"));

        mockMvc.perform(get("/media/product/prod-77"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("img-10"))
                .andExpect(jsonPath("$[1]").value("img-11"));
    }

    @Test
    void uploadImagesReturnsCreatedMediaResponsesForAuthenticatedUser() throws Exception {
        Media uploaded = Media.builder()
                .id("img-9")
                .imagePath("/uploads/img-9.png")
                .userId("user-7")
                .status(FileStatus.PENDING)
                .createdAt(Instant.parse("2026-03-26T10:00:00Z"))
                .build();

        given(jwtUtil.validateToken(TOKEN)).willReturn(true);
        given(jwtUtil.extractUserId(TOKEN)).willReturn("user-7");
        given(jwtUtil.extractEmail(TOKEN)).willReturn("seller@buy01.com");
        given(jwtUtil.extractRole(TOKEN)).willReturn("SELLER");
        given(mediaUseCase.uploadImage(any(MultipartFile.class), eq("user-7"))).willReturn(uploaded);

        mockMvc.perform(multipart("/media")
                        .file("files", "fake-image".getBytes(StandardCharsets.UTF_8))
                        .with(request -> {
                            request.addHeader(HttpHeaders.AUTHORIZATION, bearer(TOKEN));
                            request.setMethod("POST");
                            return request;
                        }))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].imagesId").value("img-9"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(mediaUseCase).uploadImage(any(MultipartFile.class), eq("user-7"));
    }

    @Test
    void deleteImagesDeletesMediaForAuthenticatedUser() throws Exception {
        given(jwtUtil.validateToken(TOKEN)).willReturn(true);
        given(jwtUtil.extractUserId(TOKEN)).willReturn("user-7");
        given(jwtUtil.extractEmail(TOKEN)).willReturn("seller@buy01.com");
        given(jwtUtil.extractRole(TOKEN)).willReturn("SELLER");

        mockMvc.perform(delete("/media/img-4")
                        .header(HttpHeaders.AUTHORIZATION, bearer(TOKEN)))
                .andExpect(status().isNoContent());

        verify(mediaUseCase).deleteById("img-4", "user-7");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
