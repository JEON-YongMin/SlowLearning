package greensnail_backend.GreenSnail.login.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserInfo {
    private String providerId;  // 카카오 고유 ID
    private String nickname;    // 카카오 닉네임
}