package com.javatechie.controller;

import com.javatechie.entity.User;
import com.javatechie.repo.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:63342", allowCredentials = "true")
public class UserController {

    private final UserRepository userRepository;
    public UserController(UserRepository userRepository){ this.userRepository = userRepository; }

    @PostMapping
    public User createUser(@RequestBody User user){
        return userRepository.save(user);
    }
}
