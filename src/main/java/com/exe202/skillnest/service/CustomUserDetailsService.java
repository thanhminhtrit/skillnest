package com.exe202.skillnest.service;

import com.exe202.skillnest.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface CustomUserDetailsService extends UserDetailsService {
    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

    User loadUserEntityByEmail(String email);
}
