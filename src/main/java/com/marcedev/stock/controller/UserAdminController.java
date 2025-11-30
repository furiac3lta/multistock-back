package com.marcedev.stock.controller;

import com.marcedev.stock.dto.UserDto;
import com.marcedev.stock.entity.Branch;
import com.marcedev.stock.entity.Role;
import com.marcedev.stock.entity.User;
import com.marcedev.stock.repository.BranchRepository;
import com.marcedev.stock.repository.RoleRepository;
import com.marcedev.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final BranchRepository branchRepo;
    private final PasswordEncoder encoder;

    @GetMapping
    public java.util.List<UserDto> all() {
        return userRepo.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @PostMapping
    public UserDto create(
            @RequestParam String username,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam Long branchId,
            @RequestParam String role
    ) {
        if (userRepo.existsByUsername(username)) {
            throw new RuntimeException("El usuario ya existe");
        }

        Branch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        Role roleEntity = roleRepo.findByName(role)
                .orElseThrow(() -> new RuntimeException("Rol no válido"));

        User user = User.builder()
                .username(username)
                .fullName(fullName)
                .phone(phone)
                .password(encoder.encode(password))
                .branch(branch)
                .roles(Set.of(roleEntity))
                .active(true)
                .build();

        userRepo.save(user);
        return toDto(user);
    }

    @PutMapping("/{id}")
    public UserDto update(
            @PathVariable Long id,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam Long branchId
    ) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Branch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        user.setFullName(fullName);
        user.setPhone(phone);
        user.setBranch(branch);

        userRepo.save(user);
        return toDto(user);
    }

    @PutMapping("/{id}/roles")
    public UserDto changeRoles(
            @PathVariable Long id,
            @RequestParam String role
    ) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Role r = roleRepo.findByName(role)
                .orElseThrow(() -> new RuntimeException("Rol no válido"));

        user.setRoles(Set.of(r));
        userRepo.save(user);

        return toDto(user);
    }

    @PutMapping("/{id}/branch")
    public UserDto changeBranch(
            @PathVariable Long id,
            @RequestParam Long branchId
    ) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Branch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        user.setBranch(branch);
        userRepo.save(user);

        return toDto(user);
    }

    @PutMapping("/{id}/toggle")
    public UserDto toggle(@PathVariable Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setActive(!user.getActive());
        userRepo.save(user);

        return toDto(user);
    }

    @PutMapping("/{id}/password")
    public void resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword
    ) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);
    }

    private UserDto toDto(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        dto.setPhone(u.getPhone());
        dto.setActive(u.getActive());

        if (u.getBranch() != null) {
            dto.setBranchId(u.getBranch().getId());
            dto.setBranchName(u.getBranch().getName());
        }

        dto.setRoles(u.getRoles().stream().map(Role::getName).toList());
        return dto;
    }
}
