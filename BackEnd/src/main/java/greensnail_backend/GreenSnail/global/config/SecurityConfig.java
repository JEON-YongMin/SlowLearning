package greensnail_backend.GreenSnail.global.config;

import greensnail_backend.GreenSnail.login.filter.JwtValidationFilter;
import greensnail_backend.GreenSnail.login.utils.OAuth2SuccessHandler;
import greensnail_backend.GreenSnail.login.utils.OAuth2UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserServiceImpl oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtValidationFilter jwtValidationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI 경로 허용
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // 인증 관련 경로 허용 - 카카오 로그인 경로 포함
                        .requestMatchers("/oauth2/authorization/**",
                                "/login/oauth2/code/**",
                                "/login/success",
                                "/auth/login/**").permitAll()
                        // 카카오 소셜 로그인 API 허용
                        .requestMatchers("/auth/kakao").permitAll()
                        // H2 콘솔 경로 허용
                        .requestMatchers("/h2-console/**").permitAll()
                        // 테스트 API 경로 허용
                        .requestMatchers("/api/test/**").permitAll()
                        // 토큰 갱신 경로 허용
                        .requestMatchers("/api/users/reissue", "/api/auth/token").permitAll()
                        // 회원가입 및 로그인 경로 허용
                        .requestMatchers("/api/auth/register", "/api/auth/register-with-image", "/api/auth/login").permitAll()
                        // 업로드된 파일 접근 허용
                        .requestMatchers("/uploads/**").permitAll()
                        // 카카오페이 API 경로 허용
                        // payment/success는 client에서 kakaopay Api서버로 direct redirection이기 때문에 우리가 어떻게 못해요
                        .requestMatchers("/payment/success").permitAll()
                        .requestMatchers("/payment/**").authenticated()
                        // 오류 페이지 및 파비콘 허용
                        .requestMatchers("/error", "/favicon.ico").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // OAuth2 로그인 설정 - 카카오만 사용하도록 수정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        // 명시적으로 카카오 로그인 경로 지정
                        .loginPage("/oauth2/authorization/kakao")
                )
                // JWT 필터 적용 - 하지만 특정 경로는 제외
                .addFilterBefore(jwtValidationFilter, UsernamePasswordAuthenticationFilter.class);

        // H2 콘솔 사용을 위한 설정
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedOrigin("http://localhost:8080");
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Refresh-Token"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}