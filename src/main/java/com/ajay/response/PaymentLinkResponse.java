package com.ajay.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLinkResponse {
	
	private String payment_link_url;
	private String payment_link_id;
	private Long payment_order_id;
	private String razorpay_order_id;
	private String razorpay_key;
	private Long amount;
	private String currency;
		

}
