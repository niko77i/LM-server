package com.lmserver.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmserver.dto.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * 平台守卫过滤器：阻止 FB 用户访问 GG 专属路由。
 * 使用 AntPathMatcher 防路径遍历绕过。
 */
public class PlatformGuardFilter extends OncePerRequestFilter {

    private static final Set<String> GG_ONLY_PATTERNS = Set.of(
            "/api/ad-reports/**", "/api/accounts/**", "/api/mcc/**",
            "/api/products/**", "/api/scrape/**", "/api/video/**",
            "/api/youtube/**", "/api/settings/**", "/api/google-sheets/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return GG_ONLY_PATTERNS.stream().noneMatch(p -> pathMatcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            if (!principal.isDeveloper() && principal.isFbUser()) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                objectMapper.writeValue(response.getWriter(),
                        ApiResponse.fail("平台访问被拒绝"));
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
