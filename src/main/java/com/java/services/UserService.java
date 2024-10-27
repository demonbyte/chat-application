package com.java.services;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.java.Entity.User;
import com.java.repository.UserRepository;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // For encoding passwords
    
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet(); // ConcurrentHashMap is thread-safe ******

    public void saveUser(User user) {
        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public String login(User user) {
        // Check if user exists
        User existingUser = userRepository.findByUsername(user.getUsername());
        
        if (existingUser != null && passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
        	
            // Generate and return JWT token here
            return generateToken(existingUser);
        }
        return null; // Invalid credentials
    }

    private String generateToken(User user) {
        // Implement JWT token generation logic
        return "generated-jwt-token"; // Replace with actual token

    }
    
    //private final Set<String> onlineUsers = new HashSet<>();

    public void addUser(String username) {
        onlineUsers.add(username);
    }

    public void removeUser(String username) {
        onlineUsers.remove(username);	
    }

    public Set<String> getOnlineUsers() {
        return onlineUsers;
    }

}






//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import com.java.Entity.User;
//import com.java.Util.JwtUtil;
//import com.java.repository.UserRepository;
//
//@Service
//public class UserService {
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
//    public User saveUser(User user) {
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        return userRepository.save(user);
//    }
//
//    public User findByUsername(String username) {
//        return userRepository.findByUsername(username);
//    }
//
//    public boolean checkPassword(User user, String rawPassword) {
//        return passwordEncoder.matches(rawPassword, user.getPassword());
//    }
//
//    public String login(User user) {
//        User existingUser = findByUsername(user.getUsername());
//        if (existingUser != null && checkPassword(existingUser, user.getPassword())) {
//            return jwtUtil.generateToken(user.getUsername());
//        }
//        return null;
//    }
//}

