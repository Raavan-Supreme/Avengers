package com.example.demo.service;

import java.util.ArrayList;


import com.example.demo.UserRepository;
import com.example.demo.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userService;

    @Override
    public UserDetails loadUserByUsername(String mobileNo) throws UsernameNotFoundException {
        User user = userService.findByMobileNo(mobileNo);
        return new org.springframework.security.core.userdetails.User(user.getMobileNo(), user.getPassword(), new ArrayList<>());
    }

    public User loadUserByMobileNo(String mobileNo) throws UsernameNotFoundException {
        User user = userService.findByMobileNo(mobileNo);
        return user;
    }
}

