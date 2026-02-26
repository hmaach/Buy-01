package com.buy01.media.infrastructure.web.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.buy01.media.domain.ports.inbound.MediaUseCase;
import com.buy01.media.infrastructure.web.dto.MediaResponse;
import com.buy01.media.infrastructure.web.mapper.MediaMapper;

@RestController
@RequestMapping("/media")
public class MediaController {

    private final MediaUseCase mediaService;

    public MediaController(MediaUseCase muc) {
        this.mediaService = muc;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getImage(@PathVariable String id) {
        Resource resource = mediaService.getImageFile(id);
        return ResponseEntity.ok()
                .contentType(mediaService.guessContentType(resource.getFilename())) 
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<MediaResponse> uploadImages(@RequestParam("files") MultipartFile[] files) {
        return List.of(files).stream()
                .map(file -> {
                    var media = mediaService.uploadImage(file);
                    return MediaMapper.toResponse(media);
                })
                .toList();
    }

}