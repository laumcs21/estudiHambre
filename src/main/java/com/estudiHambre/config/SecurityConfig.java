package com.estudiHambre.config;

import com.estudiHambre.repository.UsuarioRepository;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.http.HttpStatus;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            UsuarioRepository usuarioRepository) {

        return correo -> {
            var usuario = usuarioRepository.findByCorreo(correo)
                    .orElseThrow(() ->
                            new org.springframework.security.core.userdetails
                                    .UsernameNotFoundException("Usuario no encontrado"));

            return User.withUsername(usuario.getCorreo())
                    .password(usuario.getContrasena())
                    .roles(usuario.getRol().name())
                    .build();
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityErrorHandler securityErrorHandler) throws Exception {

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                CookieCsrfTokenRepository.withHttpOnlyFalse()
                        )
                        .csrfTokenRequestHandler(
                                new CsrfTokenRequestAttributeHandler()
                        )
                        .ignoringRequestMatchers(
                                "/api/auth/registro"
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/registro"
                        ).permitAll()

                        .requestMatchers("/api/auth/login", "/api/auth/csrf")
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/establecimientos/mis-establecimientos"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/establecimientos",
                                "/api/establecimientos/{id}"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/menus/**"
                        ).permitAll()

                        .requestMatchers("/api/auth/**")
                        .authenticated()

                        .requestMatchers("/api/establecimientos/**")
                        .authenticated()

                        .requestMatchers("/api/menus/**")
                        .authenticated()

                        .requestMatchers("/api/admin/**").authenticated()

                        .anyRequest().denyAll()
                )
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.changeSessionId())
                )
                .exceptionHandling(exceptions -> exceptions

                        .authenticationEntryPoint((request, response, exception) ->
                                securityErrorHandler.responder(
                                        request,
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "Debes iniciar sesión para acceder a este recurso"
                                )
                        )

                        .accessDeniedHandler((request, response, exception) ->
                                securityErrorHandler.responder(
                                        request,
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "No tienes permisos para realizar esta operación"
                                )
                        )
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }
}