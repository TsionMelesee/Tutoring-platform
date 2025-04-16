package com.tutorconnect.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/css/**", "/images/**", "/users/signup", "/users/signin", "/tutors/apply", "/admin/signup","/testSaveTutor").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/users/signin")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(customAuthenticationSuccessHandler()) 
                .failureUrl("/users/signin?error=true") 
                
                .permitAll()
            ).logout(logout -> logout
                    .logoutUrl("/logout")  
                    .logoutSuccessUrl("/index")  
                    .invalidateHttpSession(true) 
                    .clearAuthentication(true)  
                    .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            var user = (com.tutorconnect.model.User) authentication.getPrincipal();
            String role = user.getRole();

            if ("tutor".equals(role) && user.isFirstLogin()) {
                response.sendRedirect("/users/change-password");
            } else if ("admin".equals(role)) {
            	response.sendRedirect("/admin/applications");

            } else if ("tutor".equals(role)) {
                response.sendRedirect("/tutors/tutor-profile2");
            } else if ("student".equals(role)) {
                response.sendRedirect("/tutors/top");
            } else {
                response.sendRedirect("/"); 
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
