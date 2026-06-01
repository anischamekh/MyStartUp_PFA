package tn.iteam.backend.service;

import tn.iteam.backend.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(String username, String password);

    LoginResponse refresh(String refreshToken);
}
