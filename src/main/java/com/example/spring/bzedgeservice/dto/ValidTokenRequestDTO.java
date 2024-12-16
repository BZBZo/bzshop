package com.example.spring.bzedgeservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ValidTokenRequestDTO {
    private String token;
}
