package tn.iteam.backend.service;

import java.util.List;
import tn.iteam.backend.dto.CreateUserRequest;
import tn.iteam.backend.dto.UpdateUserRequest;
import tn.iteam.backend.dto.UserResponse;
import tn.iteam.backend.entity.User;

public interface UserService {
    List<User> findAll();
    User findById(Long id);
    UserResponse create(CreateUserRequest request);
    UserResponse update(Long id, UpdateUserRequest request);
    void delete(Long id);
}

