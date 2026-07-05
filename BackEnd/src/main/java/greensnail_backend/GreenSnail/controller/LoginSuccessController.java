package greensnail_backend.GreenSnail.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/login")
public class LoginSuccessController {

    @GetMapping("/success")
    @ResponseBody
    public String loginSuccess(
            @RequestParam(value = "accessToken", required = false) String accessToken,
            @RequestParam(value = "refreshToken", required = false) String refreshToken) {

        if (accessToken != null && refreshToken != null) {
            return "로그인 성공!<br><br>" +
                    "Access Token: " + accessToken + "<br><br>" +
                    "Refresh Token: " + refreshToken + "<br><br>" +
                    "<p>이 토큰을 복사해서 Swagger UI 인증에 사용하세요.</p>";
        } else {
            return "로그인 성공 후 토큰 정보 없음";
        }
    }
}