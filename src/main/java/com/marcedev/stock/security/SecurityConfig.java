package com.marcedev.stock.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {


    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(cs -> cs.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Angular SSR preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth sin token
                        .requestMatchers("/auth/**").permitAll()

                        // SWAGGER (si lo usas)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // SOLO ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                                // =============================
                                //      MULTISUCURSAL
                                    // =============================

                                    // Solo ADMIN puede administrar sucursales
                                .requestMatchers("/branches/**").authenticated()

                                // Solo ADMIN puede transferir stock
                                .requestMatchers(HttpMethod.POST, "/stock/transfer/**").hasRole("ADMIN")

                                // Stock requiere login
                                .requestMatchers("/stock/**").authenticated()

                                    // Productos requiere login
                                .requestMatchers("/products/**").authenticated()

                                    // Categorías requiere login
                                .requestMatchers("/categories/**").authenticated()

                                // Resto público (si lo necesitás)
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ==========================================
    //            CORS para Angular 20 SSR
    // ==========================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("*"));   // ← IMPORTANTE para SSR
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    // ==========================================
    //          AUTH MANAGER / ENCODER
    // ==========================================
    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration conf) throws Exception {
        return conf.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}
