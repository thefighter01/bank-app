package com.fighter.filter;

import com.fighter.constants.ApplicationConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

public class JWTTokenValidatorFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = request.getHeader(ApplicationConstants.JWT_HEADER);

        if (jwt != null){

            try {

                Environment env = getEnvironment();
                if (env != null) {
                    String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                    SecretKey secretKey = hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

                    if (secretKey != null){
                        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseClaimsJws(jwt).getPayload();
                            String username = claims.get("username").toString();
                            String authorities = claims.get("authorities").toString();
                        Authentication auth = new UsernamePasswordAuthenticationToken(username, null ,
                                AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }

                }


            }catch (Exception e){
                throw new BadCredentialsException("Invalid Token Received");
            }
        }


        filterChain.doFilter(request, response);

    }

    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        String path = request.getServletPath();
        return path.equals("/user");
    }
}
