package com.minipay.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minipay.dto.ApiErrorResponse;
import com.minipay.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@ConditionalOnProperty(name = "minipay.rate-limit.enabled", havingValue = "true")
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;
    private final int transferLimit;
    private final Duration transferWindow;

    public RateLimitingInterceptor(RateLimitService rateLimitService,
                                   ObjectMapper objectMapper,
                                   @Value("${minipay.rate-limit.transfer.limit}") int transferLimit,
                                   @Value("${minipay.rate-limit.transfer.window-seconds}") long transferWindowSeconds) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
        this.transferLimit = transferLimit;
        this.transferWindow = Duration.ofSeconds(transferWindowSeconds);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String key = "rate-limit:transfer:" + currentPrincipalName();
        if (rateLimitService.allow(key, transferLimit, transferWindow)) {
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiErrorResponse.of(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Too many transfer requests. Please try again later."
        ));
        return false;
    }

    private String currentPrincipalName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "anonymous";
        }
        return authentication.getName();
    }
}
