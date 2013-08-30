package org.bbplus;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public class CorsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            response.addHeader("Access-Control-Allow-Origin", "*");
            if (request.getHeader("Access-Control-Request-Method") != null
                    && "OPTIONS".equals(request.getMethod())) {
                // CORS "pre-flight" request
                response.addHeader("Access-Control-Allow-Methods",
                        "GET, POST, PUT, DELETE");
                response.addHeader("Access-Control-Allow-Headers",
                        "X-Requested-With,Origin,Content-Type, Accept, Authorization");                
            }
            filterChain.doFilter(request, response);
    }

}