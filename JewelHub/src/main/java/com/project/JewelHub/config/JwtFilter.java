package com.project.JewelHub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.JewelHub.service.CustomUserDetailService;
import com.project.JewelHub.user.User;
import com.project.JewelHub.util.ApiResponse;
import com.project.JewelHub.util.CustomException;
import com.project.JewelHub.util.ResponseWrapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailService customUserDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {

            System.out.println(LocalDateTime.now());
            String token = jwtService.extractToken(request, "auth");
            if (token != null && !token.isEmpty()) {
                String hasConfirm = jwtService.extractToken(request, "confirm");
                jwtService.validateToken(token);
                Claims userClaim = jwtService.decodeToken(token);
                Map<String, Object> userMap = userClaim.get("user", Map.class);

                User user = new User();
                user.setUserId((Integer) userMap.get("userId"));
                user.setFirstname((String) userMap.get("firstName"));
                user.setEmail((String) userMap.get("email"));

                request.setAttribute("user", user);
                request.setAttribute("confirm", !hasConfirm.isEmpty());

                UserDetails userDetails = customUserDetailService.loadUserByUsername(user.getEmail());

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                        userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

            filterChain.doFilter(request, response);
        } catch (CustomException exception) {
            ObjectMapper objectMapper = new ObjectMapper();
            ApiResponse<String> apiResponse = new ApiResponse<>(false, null, exception.getMessage());
            response.setContentType("application/json");
            response.setStatus(exception.getStatus() | 403);
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        }
    }

    /*@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().contains("/api/auth/") || request.getServletPath().contains("/api/item")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = JwtService.extractToken(request, "auth");
        boolean valid = jwtService.validateToken(token);
        if (!valid) {
            ObjectMapper objectMapper = new ObjectMapper();
            ResponseWrapper responseWrapper = new ResponseWrapper(false, 401, "Invalid or Missing token", null);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseWrapper));
        } else {
            System.out.println(token);
            Claims claims = jwtService.decodeToken(token);
            int userId = claims.get("userId", Integer.class);
            String email = claims.get("email", String.class);


            if (request.getServletPath().contains("/api/admin")){
                ObjectMapper objectMapper = new ObjectMapper();
                ResponseWrapper responseWrapper = new ResponseWrapper(false, 401, "Not authorized", null);
                response.setContentType("application/json");
                response.getWriter().write(objectMapper.writeValueAsString(responseWrapper));
            } else {
                request.setAttribute("userId", userId);
                request.setAttribute("email", email);

                filterChain.doFilter(request, response);
            }
        }
    }*/
}