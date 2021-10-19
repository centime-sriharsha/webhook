package com.example.restservice.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

@Component
public class Filter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String basicAuthHeader = request.getHeader("authorization");
        String auth = Base64.getEncoder().encodeToString(("test:test").getBytes());

        if (!("Basic " + auth).equalsIgnoreCase(basicAuthHeader)) {
            throw new RuntimeException("Basic params doesnt match!!");
        }
        filterChain.doFilter(request, response);


    }
}
