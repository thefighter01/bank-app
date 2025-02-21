package com.fighter.filter;

import jakarta.servlet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.config.annotation.web.configurers.SecurityContextConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

@Slf4j

public class AuthoritiesLoggingAfterFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
       Authentication auth =  SecurityContextHolder.getContext().getAuthentication();

       if (auth != null) {
           log.info("User {} is successfully authenticated and has authorities {}", auth.getName(), auth.getAuthorities());
       }

       chain.doFilter(request, response);
    }
}
