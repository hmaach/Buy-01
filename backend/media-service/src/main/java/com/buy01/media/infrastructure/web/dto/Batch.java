package com.buy01.media.infrastructure.web.dto;

import java.util.List;
import java.util.Map;

public class Batch {

    public record ImageRequest(
            List<String> productIds) {
    }

    public static record ImageResponse(
            Map<String, String> images) {
    }

}