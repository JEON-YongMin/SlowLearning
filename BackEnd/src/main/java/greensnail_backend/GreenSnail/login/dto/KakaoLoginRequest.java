package greensnail_backend.GreenSnail.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoLoginRequest {

    @Schema(description = "카카오 accessToken", example = "kakao_access_token_here")
    @NotBlank(message = "카카오 accessToken은 필수입니다.")
    private String accessToken;
}