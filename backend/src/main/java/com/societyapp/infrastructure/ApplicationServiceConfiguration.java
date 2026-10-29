package com.societyapp.infrastructure;

import com.societyapp.application.mapper.PlatformNotificationViewMapper;
import com.societyapp.application.mapper.RequirementViewMapper;
import com.societyapp.application.policy.ServiceCatalogPolicy;
import com.societyapp.application.port.out.JsonCodec;
import com.societyapp.application.port.out.persistence.PlatformServiceModuleRepository;
import com.societyapp.application.port.out.persistence.RequirementRequestRepository;
import com.societyapp.application.port.out.persistence.SocietyRepository;
import com.societyapp.application.port.out.persistence.UserAccountJpaRepository;
import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.regex.Pattern;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@Configuration
public class ApplicationServiceConfiguration {
    @Bean
    ServiceCatalogPolicy serviceCatalogPolicy(
            PlatformServiceModuleRepository services, JsonCodec json) {
        return new ServiceCatalogPolicy(services, json);
    }

    @Bean
    PlatformNotificationViewMapper platformNotificationViewMapper() {
        return new PlatformNotificationViewMapper();
    }

    @Bean
    RequirementViewMapper requirementViewMapper(
            JsonCodec json,
            SocietyRepository societies,
            UserAccountJpaRepository users,
            RequirementRequestRepository requirements) {
        return new RequirementViewMapper(json, societies, users, requirements);
    }

    @Bean
    static BeanDefinitionRegistryPostProcessor frameworkFreeApplicationServices() {
        return registry -> registerApplicationServices(registry);
    }

    private static void registerApplicationServices(BeanDefinitionRegistry registry) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(
                new RegexPatternTypeFilter(
                        Pattern.compile(
                                "com\\.societyapp\\.application\\.(usecase|service)\\..*")));
        for (String applicationPackage :
                java.util.List.of(
                        "com.societyapp.application.usecase",
                        "com.societyapp.application.service")) {
            for (var candidate : scanner.findCandidateComponents(applicationPackage)) {
                String className = candidate.getBeanClassName();
                if (className == null || className.contains("$")) continue;
                String simpleName = className.substring(className.lastIndexOf('.') + 1);
                String beanName = Introspector.decapitalize(simpleName);
                if (!registry.containsBeanDefinition(beanName)) {
                    registry.registerBeanDefinition(beanName, candidate);
                }
            }
        }
    }

    @Bean
    Advisor applicationTransactionAdvisor(PlatformTransactionManager transactionManager) {
        var pointcut =
                new StaticMethodMatcherPointcut() {
                    @Override
                    public boolean matches(Method method, Class<?> targetClass) {
                        return isApplicationService(targetClass)
                                && !(targetClass.getSimpleName().equals("AuditService")
                                        && method.getName().equals("record"));
                    }
                };
        var attribute = new DefaultTransactionAttribute();
        var attributes = new MatchAlwaysTransactionAttributeSource();
        attributes.setTransactionAttribute(attribute);
        var interceptor = new TransactionInterceptor(transactionManager, attributes);
        var advisor = new DefaultPointcutAdvisor(pointcut, (MethodInterceptor) interceptor);
        advisor.setOrder(1);
        return advisor;
    }

    @Bean
    Advisor auditTransactionAdvisor(PlatformTransactionManager transactionManager) {
        var pointcut =
                new StaticMethodMatcherPointcut() {
                    @Override
                    public boolean matches(Method method, Class<?> targetClass) {
                        return targetClass.getSimpleName().equals("AuditService")
                                && method.getName().equals("record");
                    }
                };
        var attribute =
                new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        var attributes = new MatchAlwaysTransactionAttributeSource();
        attributes.setTransactionAttribute(attribute);
        var interceptor = new TransactionInterceptor(transactionManager, attributes);
        var advisor = new DefaultPointcutAdvisor(pointcut, (MethodInterceptor) interceptor);
        advisor.setOrder(0);
        return advisor;
    }

    private static boolean isApplicationService(Class<?> targetClass) {
        return targetClass.getPackageName().startsWith("com.societyapp.application.usecase")
                || targetClass.getPackageName().startsWith("com.societyapp.application.service");
    }
}
