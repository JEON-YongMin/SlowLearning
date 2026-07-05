package greensnail_backend.GreenSnail.global.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SuccessCode implements BaseCode {

    // 공통 성공 코드
    OK(200, "S000", "성공적으로 처리되었습니다."),
    CREATED(201, "S001", "리소스가 성공적으로 생성되었습니다.");

    private final int status;
    private final String code;
    private final String message;
}