package greensnail_backend.GreenSnail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponseDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 아이디", example = "sanhak123")
    private String username;

    @Schema(description = "닉네임", example = "달빛이")
    private String nickname;

    @Schema(description = "회원가입 성공 여부", example = "true")
    private boolean success;
}