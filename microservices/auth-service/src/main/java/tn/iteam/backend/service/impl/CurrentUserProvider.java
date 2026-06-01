package tn.iteam.backend.service.impl;

import org.springframework.stereotype.Component;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.security.SecurityUtil;

@Component
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public CurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireCurrentUser() {
        String username = SecurityUtil.currentUsername();
        if (username == null) {
            throw new BusinessException("Not authenticated");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Current user not found"));
    }
}

