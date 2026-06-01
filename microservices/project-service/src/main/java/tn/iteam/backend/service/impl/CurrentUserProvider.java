package tn.iteam.backend.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.common.security.JwtUserPrincipal;

@Component
public class CurrentUserProvider {

    public JwtUserPrincipal requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof JwtUserPrincipal principal)) {
            throw new BusinessException("Not authenticated");
        }
        return principal;
    }
}
