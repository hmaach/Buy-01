package com.buy01.user.domain.port.out;

import java.time.Instant;

public record TokenResult(
        String token,
        Instant expiresAt
        ) {

}
