package com.login.server.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.login.server.domain.member.jwt.JwtTokenProvider;
import com.login.server.domain.member.security.filter.JwtAuthenticationFilter;
import com.login.server.domain.member.security.oauth.CustomOAuth2UserService;
import com.login.server.domain.member.security.oauth.OAuth2LoginSuccessHandler;
import com.login.server.domain.member.security.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable);

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement((session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/signup/**", "/oauth2/**","/login/oauth2/**").permitAll()
                .requestMatchers("/api/member/findId", "/api/member/findPassword", "/api/member/findPassword/verify").permitAll()
                .requestMatchers("/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
                .requestMatchers(
                    "/upload",
                    "/api/test-sale-post/**",
                    "/api/sale-post/all/**",
                    "/api/sale-post/detail/**",
                    "/api/sale-post/top-view/**"
                ).permitAll()
                .requestMatchers("/api/health/**").permitAll() //배포 헬스체크
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/*.html",
                        "/js/**",
                        "/api/ws-chat",
                        "/app/**",
                        "/api/rooms/**",
                        "/api/kamis/all",
                        "/api/sale-post/elasticsearch",
                        "/api/rooms/**"

                ).permitAll().requestMatchers(
                        "/api/admin/**",
                        "/api/report/**",
                        "/admin/**",
                                "/css/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()
                .anyRequest().authenticated()
            )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(authorization -> authorization
                                    .authorizationRequestResolver(authorizationRequestResolver(clientRegistrationRepository)) // 2. 리졸버 연결
                                )
                    .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService))
                .successHandler(oAuth2LoginSuccessHandler));

        http.addFilterBefore(
            new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
            UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        return provider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:8081","https://beanba.store"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // credentials 필요 없는 경우 false

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
        // SecurityConfig.java 내부 또는 별도 빈(Bean)으로 등록

    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorization");

        authorizationRequestResolver.setAuthorizationRequestCustomizer(customizer -> {
            customizer.parameters(params -> {
                // 카카오용 prompt=login 파라미터 전달 허용
                params.put("prompt", "login");
                // 구글용 prompt=select_account 파라미터 전달 허용
                // params.put("prompt", "select_account"); 
                // 네이버용 auth_type=reprompt 파라미터 전달 허용
                params.put("auth_type", "reprompt");
            });
        });

        return authorizationRequestResolver;
    }
}
