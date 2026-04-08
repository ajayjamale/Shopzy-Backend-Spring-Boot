package com.ajay.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;

@Data
public class Prompt {
    @NotBlank
    private String prompt;
}
