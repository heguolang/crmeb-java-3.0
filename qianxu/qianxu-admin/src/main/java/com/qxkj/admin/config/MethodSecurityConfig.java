package com.qxkj.admin.config;

import com.qxkj.common.model.system.SystemAdmin;
import com.qxkj.common.vo.LoginUserVo;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.prepost.PreInvocationAuthorizationAdviceVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.expression.method.ExpressionBasedPreInvocationAdvice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * 方法级权限：超级管理员（roles 含 1，或持有 *:*:*）一律放行，
 * 与前端 checkPermi('*:*:*') 行为对齐，避免菜单权限未入库导致「不允许访问」。
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    @Override
    @SuppressWarnings("rawtypes")
    protected AccessDecisionManager accessDecisionManager() {
        List<AccessDecisionVoter<?>> decisionVoters = new ArrayList<>();
        // 超管优先：有通配权限或角色1直接放行
        decisionVoters.add(new SuperAdminVoter());

        ExpressionBasedPreInvocationAdvice expressionAdvice = new ExpressionBasedPreInvocationAdvice();
        expressionAdvice.setExpressionHandler(getExpressionHandler());
        decisionVoters.add(new PreInvocationAuthorizationAdviceVoter(expressionAdvice));
        decisionVoters.add(new RoleVoter());
        decisionVoters.add(new AuthenticatedVoter());
        return new AffirmativeBased(decisionVoters);
    }

    /**
     * 超管优先投票：有 *:*:* 或角色 id=1 则 ACCESS_GRANTED
     */
    public static class SuperAdminVoter implements AccessDecisionVoter<MethodInvocation> {

        @Override
        public boolean supports(ConfigAttribute attribute) {
            return true;
        }

        @Override
        public boolean supports(Class<?> clazz) {
            return MethodInvocation.class.isAssignableFrom(clazz);
        }

        @Override
        public int vote(Authentication authentication, MethodInvocation object,
                        Collection<ConfigAttribute> attributes) {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ACCESS_ABSTAIN;
            }
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if ("*:*:*".equals(authority.getAuthority())) {
                    return ACCESS_GRANTED;
                }
            }
            Object principal = authentication.getPrincipal();
            if (principal instanceof LoginUserVo) {
                SystemAdmin admin = ((LoginUserVo) principal).getUser();
                if (admin != null && admin.getRoles() != null) {
                    boolean isSuper = Stream.of(admin.getRoles().split(","))
                            .map(String::trim)
                            .anyMatch("1"::equals);
                    if (isSuper) {
                        return ACCESS_GRANTED;
                    }
                }
            }
            return ACCESS_ABSTAIN;
        }
    }
}
