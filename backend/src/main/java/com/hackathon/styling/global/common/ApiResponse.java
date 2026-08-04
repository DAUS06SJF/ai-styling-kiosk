package com.hackathon.styling.global.common;

import com.hackathon.styling.global.error.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 모든 API가 공통으로 사용하는 응답 형식.
 * 키오스크·관리자·모바일 3개 프론트가 같은 구조로 파싱할 수 있도록 통일한다.
 *
 * 성공: { "success": true, "data": { ... } }
 * 실패: { "success": false, "error": { "code": "...", "message": "..." } }
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorBody error;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return error(errorCode, errorCode.getMessage());
    }

    public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(errorCode.name(), message));
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorBody {
        private final String code;
        private final String message;
    }
}
