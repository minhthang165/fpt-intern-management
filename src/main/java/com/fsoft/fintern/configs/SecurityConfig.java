package com.fsoft.fintern.configs;

import com.fsoft.fintern.filter.LoggedInRedirectFilter;
import com.fsoft.fintern.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/assets/**","index.html#/**", "/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/webjars/**", "/api/**")
                        .permitAll()
                        .requestMatchers("/logout", "/login", "/register")
                        .anonymous()
                        .anyRequest().authenticated()
//                        .anyRequest().permitAll()

                )
                .addFilterBefore(new LoggedInRedirectFilter(), UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .permitAll()
                        .successHandler((request, response, authentication) -> response.sendRedirect("/authenticate"))
                        .failureUrl("/login?error=true")
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessHandler((request, response, authentication) -> response.sendRedirect("/login?logout"))
                        .permitAll()
                );

        return http.build();
    }


//    public class checkUserExists extends OncePerRequestFilter {
//        private List<String> ALLOWED_PATHS = Arrays.asList("/profile/edit", "/assets", "/logout", "/login", "/register");
//        @Override
//        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//                throws ServletException, IOException {
//            String path = request.getServletPath();
//            if (ALLOWED_PATHS.stream().anyMatch(path::startsWith)) {
//                filterChain.doFilter(request, response);
//                return;
//            }
//            // Get current authenticated user
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            if (authentication != null && authentication.isAuthenticated()) {
//                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
//                String email = oauth2User.getAttribute("email");
//                // Check if user exists in database
//                if (userService.getByEmail(email) != null) {
//                    // Redirect to edit profile page if user doesn't exist in database
//                    response.sendRedirect("/profile/edit");
//                    return;
//                }
//            }
//            filterChain.doFilter(request, response);
//        }
//    }
}