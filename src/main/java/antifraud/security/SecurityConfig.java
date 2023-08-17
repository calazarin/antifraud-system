package antifraud.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static antifraud.enums.UserRoleEnum.*;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    private AuthenticationEntryPoint restAuthenticationEntryPoint;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder,
                                                       UserDetailsService userDetailsService)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authenticationProvider())
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder)
                .and()
                .build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
                    .csrf().disable().headers().frameOptions().disable()
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers(HttpMethod.GET, "/api/auth/list")
                    .hasAnyRole(ADMINISTRATOR.getShortName(), SUPPORT.getShortName())
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers(HttpMethod.DELETE, "/api/auth/user/**")
                    .hasRole(ADMINISTRATOR.getShortName())
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction")
                    .hasRole(MERCHANT.getShortName())
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers(HttpMethod.DELETE,"/api/antifraud/suspicious-ip/**")
                    .hasRole(SUPPORT.getShortName())
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers("/api/antifraud/suspicious-ip")
                    .hasRole(SUPPORT.getShortName())
                .and()
                  .authorizeHttpRequests()
                    .requestMatchers("/api/antifraud/stolencard/**")
                    .hasRole(SUPPORT.getShortName())
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers("/api/antifraud/stolencard")
                    .hasRole(SUPPORT.getShortName())
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers(HttpMethod.PUT, "/api/auth/access")
                    .hasRole(ADMINISTRATOR.getShortName())
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers(HttpMethod.PUT, "/api/auth/role")
                    .hasRole(ADMINISTRATOR.getShortName())
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers(HttpMethod.GET, "/api/antifraud/history/**")
                    .hasRole(SUPPORT.getShortName())
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers(HttpMethod.GET, "/api/antifraud/history")
                    .hasRole(SUPPORT.getShortName())
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction")
                    .hasRole(SUPPORT.getShortName())
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers(HttpMethod.POST, "/api/auth/user")
                    .permitAll()
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers("/actuator/shutdown")
                    .permitAll()
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers("/swagger-ui/**")
                    .permitAll()
                .and()
                    .authorizeHttpRequests()
                    .requestMatchers("/api-docs/**")
                    .permitAll()
                .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        return http.build();
    }
}
