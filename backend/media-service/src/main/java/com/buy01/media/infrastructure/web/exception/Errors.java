package com.buy01.media.infrastructure.web.exception;

public class Errors {

    public static class Faileduploadedfile extends RuntimeException {
        public Faileduploadedfile(String message) {
            super(message);
        }
    }

    public static class NotFound extends RuntimeException {
        public NotFound(String message) {
            super(message);
        }
    }
}
