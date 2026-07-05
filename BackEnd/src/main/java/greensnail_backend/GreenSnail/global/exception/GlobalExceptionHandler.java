package greensnail_backend.GreenSnail.global.exception;

import greensnail_backend.GreenSnail.global.api.ApiResponse;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.api.ReasonDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직에서 발생한 커스텀 예외 처리
     */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }

    /**
     * 일반적인 예외 처리
     */
    @ExceptionHandler(GeneralException.class)
    protected ResponseEntity<ApiResponse<Object>> handleGeneralException(GeneralException e) {
        log.error("GeneralException: {}", e.getMessage(), e);
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }

    /**
     * 유효성 검사 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<List<ReasonDto>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage());
        List<ReasonDto> reasons = extractReasons(e.getBindingResult());
        ApiResponse<List<ReasonDto>> response = ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE, "입력값 검증에 실패했습니다.");
        response.setData(reasons);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 그 외 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    /**
     * 유효성 검사 실패 이유 추출
     */
    private List<ReasonDto> extractReasons(BindingResult bindingResult) {
        List<ReasonDto> reasons = new ArrayList<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            reasons.add(new ReasonDto(error.getField(), error.getDefaultMessage()));
        }
        return reasons;
    }
}