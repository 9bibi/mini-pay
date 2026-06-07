package com.minipay.config;

import java.util.Optional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Optional<RateLimitingInterceptor> rateLimitingInterceptor;

    public WebConfig(Optional<RateLimitingInterceptor> rateLimitingInterceptor) {
        this.rateLimitingInterceptor = rateLimitingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        rateLimitingInterceptor.ifPresent(interceptor -> registry.addInterceptor(interceptor)
                .addPathPatterns("/api/wallets/transfer"));
    }
}
