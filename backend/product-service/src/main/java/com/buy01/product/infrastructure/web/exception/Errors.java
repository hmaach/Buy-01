package com.buy01.product.infrastructure.web.exception;

public class Errors {
    public static class MediaServiceException extends RuntimeException {
        private final int statusCode;
        private final String responseBody;

        public MediaServiceException(int statusCode, String message, String responseBody) {
            super(message != null ? message : "Media service error (" + statusCode + ")");
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getResponseBody() {
            return responseBody;
        }
    }
}
