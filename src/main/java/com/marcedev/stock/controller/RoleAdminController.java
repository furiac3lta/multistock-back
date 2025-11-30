package com.marcedev.stock.controller;

import com.marcedev.stock.entity.Role;
import com.marcedev.stock.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleAdminController {

    private final RoleRepository roleRepo;

    @GetMapping
    public List<Role> all() {
        return roleRepo.findAll();
    }

    @PostMapping
    public Role create(@RequestParam String name) {
        if (roleRepo.existsByName(name)) {
            throw new RuntimeException("El rol ya existe");
        }
        Role r = Role.builder().name(name).build();
        return roleRepo.save(r);
    }
}
