package com.marcedev.stock.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserUpdateDto {

    private String fullName;
    private String phone;
    private Boolean active;
    private Long branchId;
    private List<String> roles;
}
