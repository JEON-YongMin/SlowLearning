package greensnail_backend.GreenSnail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @Schema(description = "사용자 아이디", example = "sanhak123")
    @NotBlank(message = "아이디는 필수 입력값입니다.")
    private String username;

    @Schema(description = "비밀번호", example = "sanhak123!")
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;
}