package com.example.base_spring_boot.models.dtos.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TokenRefreshResponse {
    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private final String type = "Bearer";
}
