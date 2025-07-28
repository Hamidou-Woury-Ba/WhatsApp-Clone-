package com.hamidou.whatsappclone.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponse> getAllUsersExceptSelf(Authentication currentUser) {
        String currentUserId = currentUser.getName();
        return userRepository.findAllUsersExceptSelf(currentUserId).stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

}
