package com.fighter.config;


import com.fighter.exceptionhandling.CustomAccessDeniedHandler;
import com.fighter.exceptionhandling.CustomBasicAuthenticationEntryPoint;
import com.fighter.filter.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@Profile("!prod")
public class ProjectSecurityConfig {




    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();
        http.sessionManagement(sc->sc.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.cors(corsConf ->corsConf.configurationSource(new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration corsConfiguration = new CorsConfiguration();
                corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
                corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
                corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
                corsConfiguration.setExposedHeaders(Arrays.asList("Authorization"));
                corsConfiguration.setAllowCredentials(true);
                corsConfiguration.setMaxAge(3600L);
                return corsConfiguration;
            }
        })).csrf(csrfConfig-> csrfConfig.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
                                .ignoringRequestMatchers(
                "/register" , "/contact", "/apiLogin")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new RequestValidtationfilter() , BasicAuthenticationFilter.class)
                .addFilterAfter(new AuthoritiesLoggingAfterFilter(), BasicAuthenticationFilter.class)
                .addFilterAt(new AuthoritiesLoggingAtFilter() , BasicAuthenticationFilter.class)
                .addFilterBefore(new JWTTokenValidatorFilter() , BasicAuthenticationFilter.class)
                .addFilterAfter(new JWTTokenGeneratorFilter() , BasicAuthenticationFilter.class)

        .requiresChannel(rcc-> rcc.anyRequest().requiresInsecure())
                .authorizeHttpRequests((requests) ->
                requests.requestMatchers( "/myAccount" ).hasRole("USER")
                        .requestMatchers( "/myBalance").hasAnyRole("USER" , "ADMIN")
                        .requestMatchers( "/myLoans" ).authenticated()
                        .requestMatchers("/myCards" ).hasRole("USER")
                        .requestMatchers( "/user").authenticated()
                .requestMatchers("/notices" , "/contact" , "/error" , "/register" ,
                        "/invalidSession" , "/error", "/test","/custom" , "/apiLogin").permitAll());

        http.formLogin(withDefaults());
        http.httpBasic( hbc-> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
       http.exceptionHandling(ehc->ehc.accessDeniedHandler(new CustomAccessDeniedHandler()));



        return http.build();
    }


//    @Bean
//    public UserDetailsService userDetailsService(DataSource dataSource) {
//        return new JdbcUserDetailsManager(dataSource);
//    }
//


    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    @Bean public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService , PasswordEncoder encoder) throws Exception {

        BankUsernamePasswordAuthenticationProvider authenticationProvider = new BankUsernamePasswordAuthenticationProvider(userDetailsService, encoder);
        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);
        return providerManager;

    }




}
