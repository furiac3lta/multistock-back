package com.marcedev.stock.controller;

import com.marcedev.stock.dto.*;
import com.marcedev.stock.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    public List<UserDto> all() {
        return service.findAll();
    }

    @PostMapping
    public UserDto create(@RequestBody UserCreateDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UserUpdateDto dto) {
        return service.update(id, dto);
    }

    @PutMapping("/{id}/password")
    public void changePassword(@PathVariable Long id, @RequestBody String newPassword) {
        service.changePassword(id, newPassword);
    }
}
