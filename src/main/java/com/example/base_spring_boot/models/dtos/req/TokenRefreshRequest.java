package com.example.base_spring_boot.models.dtos.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TokenRefreshRequest {
    @NotBlank(message = "refreshToken không được để trống")
    private String refreshToken;
}
