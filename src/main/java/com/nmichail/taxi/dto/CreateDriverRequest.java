package com.nmichail.taxi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDriverRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Email @Size(max = 320) String email,
        @Size(max = 50) String phone,
        @NotBlank @Size(max = 100) String licenseNumber
) {
}