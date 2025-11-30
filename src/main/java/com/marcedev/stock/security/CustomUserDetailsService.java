package com.marcedev.stock.security;

import com.marcedev.stock.entity.User;
import com.marcedev.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())     // BCrypt!
                .authorities(user.getRoles().stream()
                        .map(r -> "ROLE_" + r.getName())   // ← IMPORTANTE
                        .toArray(String[]::new))
                .accountLocked(!user.getActive())
                .build();
    }
}
