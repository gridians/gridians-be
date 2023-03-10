package com.devember.global.config.security.filter;

import com.devember.global.config.security.service.CustomUserDetailsService;
import com.devember.global.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService customUserDetailsService;

    private RequestMatcher requestMatcher = new AntPathRequestMatcher("/user/**");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!requestMatcher.matches(request)) {
            try {
                String jwt = getResolveAuthHeader(request);

                if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {
                    String email = jwtUtils.getUserEmailFromToken(jwt);

                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (Exception exception) {
                throw new RuntimeException("jwt token error");
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getResolveAuthHeader(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if(StringUtils.hasText(auth) && auth.startsWith("Bearer")){
            return auth.substring(7);
        }
        return null;
    }
}
