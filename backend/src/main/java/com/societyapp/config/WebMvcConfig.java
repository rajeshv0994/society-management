package com.societyapp.config;

import com.societyapp.security.AuditMutationInterceptor;
import com.societyapp.security.ServiceEntitlementInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final ServiceEntitlementInterceptor serviceEntitlements;
    private final AuditMutationInterceptor auditMutations;

    public WebMvcConfig(
            ServiceEntitlementInterceptor serviceEntitlements,
            AuditMutationInterceptor auditMutations) {
        this.serviceEntitlements = serviceEntitlements;
        this.auditMutations = auditMutations;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(serviceEntitlements).addPathPatterns("/api/**");
        registry.addInterceptor(auditMutations).addPathPatterns("/api/**");
    }
}
