package com.fighter.filter;

import com.fighter.constants.ApplicationConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;


public class JWTTokenGeneratorFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null){
            Environment env = getEnvironment();
            if (env != null)  {
                String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY , ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

                String jwt = Jwts.builder().issuer("Bank").subject("JWT Token")
                        .claim("username", auth.getName()).claim("authorities", auth.getAuthorities().stream().
                                map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                        .issuedAt(new java.util.Date())
                        .expiration(new Date((new Date()).getTime() + 8 * 60 * 60 * 1000))
                        .signWith(secretKey).compact();

                response.setHeader(ApplicationConstants.JWT_HEADER, jwt);
            }


        }
        filterChain.doFilter(request, response);

    }

    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        String path = request.getServletPath();
        return !path.equals("/user");
    }
}
