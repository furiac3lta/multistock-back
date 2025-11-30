package com.marcedev.stock.service;

import com.marcedev.stock.dto.*;

import java.util.List;

public interface UserService {

    List<UserDto> findAll();

    UserDto create(UserCreateDto dto);

    UserDto update(Long id, UserUpdateDto dto);

    void changePassword(Long id, String newPassword);
}
