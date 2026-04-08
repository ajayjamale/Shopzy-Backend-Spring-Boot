package com.ajay.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
	
	@NotBlank
	@Size(min = 8)
	private String password;

	@NotBlank
	private String token;


}
