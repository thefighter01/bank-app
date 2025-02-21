package com.fighter.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.util.StringUtils;

public class RequestValidtationfilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest)  request;
        HttpServletResponse res = (HttpServletResponse) response;

        String header = req.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null){
            header = header.trim();
            if (StringUtils.startsWithIgnoreCase(header, "Basic ")){

                byte [] base64Token = header.substring(6).getBytes(StandardCharsets.UTF_8);
                byte [] decoded;

                try {
                    decoded = Base64.decode(base64Token);
                    String token = new String(decoded, StandardCharsets.UTF_8);

                    int index = token.indexOf(":");
                    if (index == -1) {
                        throw new BadCredentialsException("Invalid basic authentication token");
                    }

                    String email = token.substring(0 , index);

                    if (email.toLowerCase().contains("test")){
                        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    }

                }
                catch (Exception e) {
                    throw new BadCredentialsException("Faild to decode basic authentication token");

            }
        }



        }
        chain.doFilter(request, response);

    }
}
