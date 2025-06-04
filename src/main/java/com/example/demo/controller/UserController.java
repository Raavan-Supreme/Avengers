package com.example.demo.controller;

import com.example.demo.UserRepository;
import com.example.demo.dto.*;
import com.example.demo.util.JwtTokenUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/v1")
@Transactional
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenPayload jwtTokenPayload;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @PostMapping(path = "loginWithEmail", consumes = "application/json")
    public ApiResponse loginWithEmail(@Valid @RequestBody LoginWithEmailDto request) {
        try {
            User oldUser = userRepository.findByEmail(request.getEmail());

            if (oldUser != null) {
                if (new BCryptPasswordEncoder().matches(request.getPassword(), oldUser.getPassword())) {

                    // Create user Token
                    final UserDetails userDetails = userDetailsService.loadUserByUsername(oldUser.getEmail());
                    Map<String, Object> claims = new HashMap<>();
                    Map<String, Object> res = new HashMap<>();

                    jwtTokenPayload.setUserId(oldUser.getUuid());
                    jwtTokenPayload.setEmail(oldUser.getEmail());

                    claims.put("jti", jwtTokenPayload);

                    final String token = jwtTokenUtil.generateToken(userDetails, claims, 6000000);
                    res.put("token", token);
                    res.put("user_id", oldUser.getUuid());
                    res.put("email", oldUser.getEmail());
                    return new ApiResponse(false, "LOGIN_SUCCESSFULLY", HttpStatus.OK, res);
                } else {
                    return new ApiResponse(true, "INVALID_CREDIENTIALS",
                            HttpStatus.UNAUTHORIZED, null);
                }

            } else {
                return new ApiResponse(true, "USER_NOT_FOUND",
                        HttpStatus.UNAUTHORIZED, null);
            }

        } catch (Exception e) {
            log.error("Error :: {}", e.getLocalizedMessage());
            return new ApiResponse(true, e.getMessage(), HttpStatus.EXPECTATION_FAILED, null);
        }
    }
    @PostMapping(path = "registrationWithEmail")
    public ApiResponse registrationWithEmail(@Valid @RequestBody RegisterWithEmailDto request) {
        try {
            log.info("registrationWithEmail api called");
            User checkUserByEmail = userRepository.findByEmail(request.getEmail());
            if (checkUserByEmail != null) {
                return new ApiResponse(true, "EMAIL_IS_ALREADY_REGISTERED",
                        HttpStatus.PRECONDITION_FAILED, null);
            }

            User user = new User();
            String userId = String.valueOf(UUID.randomUUID());

                if (request.getName() == null) {
                    return new ApiResponse(true, "NAME_SHOULD_NOT_BE_BLANK",
                            HttpStatus.PRECONDITION_FAILED,
                            null);
                }
                user.setName(request.getName());

            user.setUuid(userId);
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(new BCryptPasswordEncoder().encode(request.getPassword()));
            userRepository.save(user);
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            Map<String, Object> claims = new HashMap<>();
            Map<String, Object> res = new HashMap<>();

            jwtTokenPayload.setUserId(user.getUuid());
            jwtTokenPayload.setEmail(user.getEmail());

            claims.put("jti", jwtTokenPayload);

            final String token = jwtTokenUtil.generateToken(userDetails, claims, 6000000);
            res.put("token", token);
            res.put("user_id", user.getUuid());
            res.put("email", user.getEmail());

            return new ApiResponse(false, "REGISTRATION_SUCCESSFULLY", HttpStatus.OK, res);

        } catch (Exception e) {
            log.error("Error :: {}", e.getLocalizedMessage());
            return new ApiResponse(true, e.getMessage(), HttpStatus.EXPECTATION_FAILED, null);
        }
    }
}
