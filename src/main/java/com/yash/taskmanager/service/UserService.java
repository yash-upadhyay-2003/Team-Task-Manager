package com.yash.taskmanager.service;

import com.yash.taskmanager.dto.UserDTO;
import com.yash.taskmanager.entity.User;
import com.yash.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getRole().name()))
                .collect(Collectors.toList());
    }
}