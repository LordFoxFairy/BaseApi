package org.foxfairy.base.api.core.common;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class HttpResponse<T> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final HttpStatus status;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;
    private final Integer code;

    private HttpResponse(HttpStatus status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.code = status.value();
    }

    private HttpResponse(HttpStatus status, String message, T data, Integer code) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.code = code;
    }

    public static <T> HttpResponse<T> ok(HttpStatus status, T data) {
        return HttpResponse.success(status, data);
    }

    public static <T> HttpResponse<T> ok(T data) {
        return HttpResponse.success(data);
    }

    public static <T> HttpResponse<T> ok200(T data) {
        return HttpResponse.success(data);
    }

    public static <T> HttpResponse<T> success(T data) {
        return HttpResponse.success(HttpStatus.OK, data);
    }

    public static <T> HttpResponse<T> success(HttpStatus status, T data) {
        return HttpResponse.template(status, data);
    }

    public static <T> HttpResponse<T> success(T data, String message) {
        return HttpResponse.template(HttpStatus.OK, data, message);
    }

    public static <T> HttpResponse<T> template(HttpStatus status, T data) {
        return new HttpResponse<>(status, status.getReasonPhrase(), data);
    }

    public static <T> HttpResponse<T> template(HttpStatus status, T data, String message) {
        return new HttpResponse<>(status, message, data);
    }

    public static <T> HttpResponse<T> error(HttpStatus status, String message) {
        return new HttpResponse<>(status, message, null);
    }

    public static <T> HttpResponse<T> error(Integer code, String message) {
        return new HttpResponse<>(null, message, null, code);
    }

    public static <T> HttpResponse<T> error404(String message) {
        return new HttpResponse<>(HttpStatus.NOT_FOUND, message, null);
    }

    public static <T> HttpResponse<T> error500(String message) {
        return new HttpResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
    }
}

