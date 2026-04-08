package com.ajay.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequest {

	@NotNull
	@Positive
	private Long productId;
	private String size;

	@Positive
	private int quantity;

	@Positive
	private Integer price;
	

	
}
