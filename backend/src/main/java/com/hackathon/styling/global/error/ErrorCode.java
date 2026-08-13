package com.hackathon.styling.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 서비스 전역 에러 코드.
 * 도메인별로 구간을 나눠 쓰고, 새 코드가 필요하면 해당 구간에 추가한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 스타일링 (이승원)
    STYLING_NOT_FOUND(HttpStatus.NOT_FOUND, "스타일링 정보를 찾을 수 없습니다."),
    STYLING_CANDIDATE_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY, "추천할 수 있는 재고 상품이 없습니다."),
    OPENAI_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "OpenAI API 키가 설정되지 않았습니다."),
    STYLING_GENERATION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "AI 스타일링 생성에 실패했습니다."),

    // 상품 / 재고 / 센서 (서의진)
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품 정보를 찾을 수 없습니다."),
    DUPLICATE_HANGER_CODE(HttpStatus.CONFLICT, "이미 등록된 옷걸이 코드입니다."),
    HANGER_NOT_FOUND(HttpStatus.NOT_FOUND, "등록되지 않은 행거입니다."),
    SHARE_LINK_EXPIRED(HttpStatus.GONE, "만료된 공유 링크입니다.");

    private final HttpStatus status;
    private final String message;
}
