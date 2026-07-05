package greensnail_backend.GreenSnail.global.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode implements BaseCode {

    // 공통 에러
    INVALID_INPUT_VALUE(400, "C001", "유효하지 않은 입력값입니다."),
    METHOD_NOT_ALLOWED(405, "C002", "지원하지 않는 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(404, "C003", "데이터를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "C004", "서버 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(400, "C005", "유효하지 않은 유형입니다."),
    ACCESS_DENIED(403, "C006", "접근이 거부되었습니다."),
    UNAUTHORIZED(401, "C007", "인증이 필요합니다."),
    TOKEN_EXPIRED(401, "C008", "만료된 토큰입니다."),

    // 글 요약 관련 에러
    USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),
    ARTICLE_NOT_FOUND(404, "A001", "글을 찾을 수 없습니다."),
    SUMMARY_NOT_FOUND(404, "S001", "요약을 찾을 수 없습니다."),

    // 커뮤니티 관련 에러
    POST_NOT_FOUND(404, "C009", "게시글이 존재하지 않습니다."),
    COMMENT_NOT_FOUND(404, "C010", "댓글이 존재하지 않습니다.");

    private final int status;
    private final String code;
    private final String message;
}
