package com.smartexpense.service;

import com.smartexpense.dto.RegisterDTO;
import com.smartexpense.entity.User;

public interface UserService {

    User register(RegisterDTO dto);

    /** Returns the currently authenticated user, or throws if none is authenticated. */
    User getCurrentUser();

    boolean usernameExists(String username);

    boolean emailExists(String email);
}
