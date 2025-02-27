package com.fighter.config;


import com.fighter.exceptionhandling.CustomAccessDeniedHandler;
import com.fighter.filter.CsrfCookieFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@Profile("!prod")
public class ProjectSecurityConfig {
//
//    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-uri}")
//    String introspectionUri;
//
//    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-id}")
//    String clientId;
//
//    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-secret}")
//    String clientSecret;





    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

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


        .requiresChannel(rcc-> rcc.anyRequest().requiresInsecure())
                .authorizeHttpRequests((requests) ->
                requests.requestMatchers( "/myAccount" ).hasRole("USER")
                        .requestMatchers( "/myBalance").hasAnyRole("USER" , "ADMIN")
                        .requestMatchers( "/myLoans" ).authenticated()
                        .requestMatchers("/myCards" ).hasRole("USER")
                        .requestMatchers( "/user").authenticated()
                .requestMatchers("/notices" , "/contact" , "/error" , "/register" ,
                        "/invalidSession" , "/error", "/test","/custom" , "/apiLogin").permitAll());

       http.oauth2ResourceServer(osr->
               osr.jwt(jwt->
                       jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
//        http.oauth2ResourceServer(osr->
//                osr.opaqueToken(opq->opq
//                        .introspectionClientCredentials(clientId, clientSecret)
//                        .authenticationConverter(new KeycloakOpaqueRoleConverter()).
//                        introspectionUri(introspectionUri)));
       http.exceptionHandling(ehc->ehc.accessDeniedHandler(new CustomAccessDeniedHandler()));



        return http.build();
    }





}
