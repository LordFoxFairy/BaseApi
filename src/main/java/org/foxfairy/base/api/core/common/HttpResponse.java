package org.foxfairy.base.api.core.common;

import org.springframework.http.HttpStatus;

public class HttpResponse<T> {

    private final HttpStatus status;
    private final String message;
    private final T data;

    private HttpResponse(HttpStatus status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> HttpResponse<T> success(T data) {
        return new HttpResponse<>(HttpStatus.OK, "Success", data);
    }

    public static <T> HttpResponse<T> success(T data, HttpStatus status) {
        return new HttpResponse<>(status, "Success", data);
    }

    public static <T> HttpResponse<T> error(HttpStatus status, String message) {
        return new HttpResponse<>(status, message, null);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}

