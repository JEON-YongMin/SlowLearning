package greensnail_backend.GreenSnail.login.mapper;

import greensnail_backend.GreenSnail.login.dto.JwtDto;

public class UserConverter {

    // JWT 토큰 DTO 변환
    public static JwtDto jwtDto(String access, String refresh) {
        return JwtDto.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .build();
    }
}