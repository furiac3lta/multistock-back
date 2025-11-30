package com.marcedev.stock.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserCreateDto {

    private String username;
    private String password;

    private String fullName;
    private String phone;

    private Boolean active = true;

    private Long branchId;

    private List<String> roles;
}
