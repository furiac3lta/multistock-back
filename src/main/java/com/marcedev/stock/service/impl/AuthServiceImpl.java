package com.marcedev.stock.service.impl;

import com.marcedev.stock.dto.AuthRequestDto;
import com.marcedev.stock.dto.AuthResponseDto;
import com.marcedev.stock.entity.User;
import com.marcedev.stock.exception.ApiException;
import com.marcedev.stock.repository.UserRepository;
import com.marcedev.stock.security.JwtService;
import com.marcedev.stock.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;

    @Override
    public AuthResponseDto login(AuthRequestDto dto) {

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        if (Boolean.FALSE.equals(user.getActive())) {
            throw new ApiException("Usuario inactivo");
        }
        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String token = jwtService.generateToken(user);

        AuthResponseDto resp = new AuthResponseDto();
        resp.setToken(token);
        resp.setUsername(user.getUsername());
        resp.setRoles(
                user.getRoles()
                        .stream()
                        .map(r -> r.getName())
                        .toList()
        );

        return resp;
    }
}
