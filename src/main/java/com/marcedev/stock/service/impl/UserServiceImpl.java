package com.marcedev.stock.service.impl;

import com.marcedev.stock.dto.*;
import com.marcedev.stock.entity.Branch;
import com.marcedev.stock.entity.Role;
import com.marcedev.stock.entity.User;
import com.marcedev.stock.repository.BranchRepository;
import com.marcedev.stock.repository.RoleRepository;
import com.marcedev.stock.repository.UserRepository;
import com.marcedev.stock.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final BranchRepository branchRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    @Override
    public List<UserDto> findAll() {
        return userRepo.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public UserDto create(UserCreateDto dto) {

        if (userRepo.existsByUsername(dto.getUsername()))
            throw new RuntimeException("El usuario ya existe");

        Branch branch = branchRepo.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        // ✔ CORREGIDO – convertir Optional<Role> → Role
        Set<Role> roleEntities = dto.getRoles().stream()
                .map(name -> roleRepo.findByName(name)
                        .orElseThrow(() -> new RuntimeException("Rol no existe: " + name)))
                .collect(java.util.stream.Collectors.toSet());

        User user = User.builder()
                .username(dto.getUsername())
                .password(encoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .active(dto.getActive())
                .branch(branch)
                .roles(roleEntities)
                .build();

        userRepo.save(user);
        return toDto(user);
    }

    @Override
    public UserDto update(Long id, UserUpdateDto dto) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (dto.getFullName() != null)
            user.setFullName(dto.getFullName());

        if (dto.getPhone() != null)
            user.setPhone(dto.getPhone());

        if (dto.getActive() != null)
            user.setActive(dto.getActive());

        if (dto.getBranchId() != null) {
            Branch branch = branchRepo.findById(dto.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
            user.setBranch(branch);
        }

        if (dto.getRoles() != null) {
            Set<Role> roleEntities = dto.getRoles().stream()
                    .map(name -> roleRepo.findByName(name)
                            .orElseThrow(() -> new RuntimeException("Rol no existe: " + name)))
                    .collect(java.util.stream.Collectors.toSet());
            user.setRoles(roleEntities);
        }

        userRepo.save(user);
        return toDto(user);
    }

    @Override
    public void changePassword(Long id, String newPassword) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);
    }

    // =====================================
    // HELPERS
    // =====================================
    private UserDto toDto(User user) {

        UserDto dto = new UserDto();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setActive(user.getActive());

        if (user.getBranch() != null) {
            dto.setBranchId(user.getBranch().getId());
            dto.setBranchName(user.getBranch().getName());
        }

        dto.setRoles(
                user.getRoles().stream()
                        .map(Role::getName)
                        .toList()
        );

        return dto;
    }
}
