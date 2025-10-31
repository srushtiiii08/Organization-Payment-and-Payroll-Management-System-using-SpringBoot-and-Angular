package com.aurionpro.payroll.dto.response;

import com.aurionpro.payroll.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

	private String token;
    private String email;
    private Role role;
    private Long userId;
    private String message;
}
