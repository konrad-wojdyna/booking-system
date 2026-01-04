package com.booking.bookingsystem.security.filter;

import com.booking.bookingsystem.security.jwt.JwtUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {

        try{

            String authHeader = request.getHeader("Authorization");
            String token = null;

            if(authHeader != null && authHeader.startsWith("Bearer ")){
                token = authHeader.substring(7);
            }

           if(token == null){
               filterChain.doFilter(request, response);
               return;
           }

           String email = jwtUtils.extractEmail(token);

           if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
               UserDetails userDetails = userDetailsService.loadUserByUsername(email);

               if(jwtUtils.isTokenValid(token, userDetails.getUsername())){
                   log.debug("JWT token valid for user: {}", email);

                   UsernamePasswordAuthenticationToken authToken =
                           new UsernamePasswordAuthenticationToken(
                                   userDetails,
                                   null,
                                   userDetails.getAuthorities()
                           );

                   authToken.setDetails(
                           new WebAuthenticationDetailsSource().buildDetails(request)
                   );

                   SecurityContextHolder.getContext().setAuthentication(authToken);
               }
           }

        }catch (JwtException e){
            log.error("JWT token validation failed: {}", e.getMessage());
        }catch (Exception e){
            log.error("Error processing JWT token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
