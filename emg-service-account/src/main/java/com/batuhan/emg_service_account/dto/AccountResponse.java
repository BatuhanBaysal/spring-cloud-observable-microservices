package com.batuhan.emg_service_account.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccountResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private boolean isAccountActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}