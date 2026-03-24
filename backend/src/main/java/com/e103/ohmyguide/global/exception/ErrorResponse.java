package com.e103.ohmyguide.global.exception;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ErrorResponse {

    private int status;
    private String message;
    private List<FieldError> errors;

    public static ErrorResponse of(int status, String message) {
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .errors(List.of())
                .build();
    }

    public static ErrorResponse of(int status, String message, List<FieldError> errors) {
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .errors(errors)
                .build();
    }

    @Getter
    @Builder
    public static class FieldError {
        private String field;
        private String value;
        private String reason;

        public static FieldError of(String field, String value, String reason) {
            return FieldError.builder()
                    .field(field)
                    .value(value)
                    .reason(reason)
                    .build();
        }
    }
}
