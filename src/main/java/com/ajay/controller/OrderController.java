package com.ajay.controller;

import com.ajay.domains.PaymentMethod;
import com.ajay.domains.PaymentStatus;
import com.ajay.exception.OrderException;
import com.ajay.exception.SellerException;
import com.ajay.exception.UserException;
import com.ajay.model.*;
import com.ajay.repository.PaymentOrderRepository;
import com.ajay.payload.response.PaymentLinkResponse;
import com.ajay.service.*;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
	
	private final OrderService orderService;
	private final UserService userService;
	private final OrderItemService orderItemService;
	private final CartService cartService;
	private final PaymentService paymentService;
	private final PaymentOrderRepository paymentOrderRepository;
	private final SellerReportService sellerReportService;
	private final SellerService sellerService;

	
	@PostMapping()
	public ResponseEntity<PaymentLinkResponse> createOrderHandler(
			@RequestBody Address spippingAddress,
			@RequestParam PaymentMethod paymentMethod,
			@RequestHeader("Authorization")String jwt)
            throws UserException, RazorpayException, OrderException {
		
		User user=userService.findUserProfileByJwt(jwt);
		Cart cart=cartService.findUserCart(user);
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new OrderException("Cart is empty");
        }
		Set<Order> orders =orderService.createOrder(user, spippingAddress,cart);

		PaymentOrder paymentOrder=paymentService.createOrder(user,orders);
        paymentOrder.setPaymentMethod(paymentMethod);
        paymentOrder = paymentOrderRepository.save(paymentOrder);

		PaymentLinkResponse res = new PaymentLinkResponse();
        res.setPayment_order_id(paymentOrder.getId());

		if(paymentMethod.equals(PaymentMethod.RAZORPAY)){
			com.razorpay.Order razorpayOrder = paymentService.createRazorpayOrder(
					user,
					paymentOrder.getAmount(),
					paymentOrder.getId()
			);
			String razorpayOrderId = razorpayOrder.get("id");
			Long amountInPaise = toLongValue(razorpayOrder.get("amount"), "amount");
			String currency = razorpayOrder.get("currency");

			paymentOrder.setRazorpayOrderId(razorpayOrderId);
			paymentOrderRepository.save(paymentOrder);

			res.setRazorpay_order_id(razorpayOrderId);
			res.setAmount(amountInPaise);
			res.setCurrency(currency);
			res.setRazorpay_key(paymentService.getRazorpayKey());
		}
		
		return new ResponseEntity<>(res,HttpStatus.OK);

	}

	private Long toLongValue(Object value, String fieldName) {
		if (value instanceof Number numberValue) {
			return numberValue.longValue();
		}
		if (value instanceof String stringValue && !stringValue.isBlank()) {
			try {
				return Long.parseLong(stringValue);
			} catch (NumberFormatException ignored) {
				// Fall through to throw a uniform error below.
			}
		}
		throw new IllegalStateException("Unexpected type for Razorpay field '" + fieldName + "'");
	}
	
	@GetMapping("/user")
	public ResponseEntity< List<Order>> usersOrderHistoryHandler(
			@RequestHeader("Authorization")
	String jwt) throws UserException{
		
		User user=userService.findUserProfileByJwt(jwt);
		List<Order> orders=orderService.usersOrderHistory(user.getId());
		return new ResponseEntity<>(orders,HttpStatus.ACCEPTED);
	}
	
	@GetMapping("/{orderId}")
	public ResponseEntity< Order> getOrderById(@PathVariable Long orderId, @RequestHeader("Authorization")
	String jwt) throws OrderException, UserException{
		
			User user = userService.findUserProfileByJwt(jwt);
			Order orders=orderService.findOrderById(orderId);
	        if (!canAccessOrder(user, orders)) {
	            throw new OrderException("you can't access this order " + orderId);
	        }
            if (!isRole("ROLE_ADMIN") && orders.getPaymentStatus() != PaymentStatus.COMPLETED) {
                throw new OrderException("Order not found " + orderId);
            }
			return new ResponseEntity<>(orders,HttpStatus.ACCEPTED);
	}

	@GetMapping("/item/{orderItemId}")
	public ResponseEntity<OrderItem> getOrderItemById(
			@PathVariable Long orderItemId, @RequestHeader("Authorization")
	String jwt) throws Exception {
			User user = userService.findUserProfileByJwt(jwt);
			OrderItem orderItem=orderItemService.getOrderItemById(orderItemId);
	        if (!user.getId().equals(orderItem.getUserId()) && !isRole("ROLE_ADMIN")) {
	            throw new OrderException("you can't access this order item " + orderItemId);
	        }
            if (!isRole("ROLE_ADMIN") && orderItem.getOrder().getPaymentStatus() != PaymentStatus.COMPLETED) {
                throw new OrderException("Order item not found " + orderItemId);
            }
			return new ResponseEntity<>(orderItem,HttpStatus.ACCEPTED);
	}

	@PutMapping("/{orderId}/cancel")
	public ResponseEntity<Order> cancelOrder(
			@PathVariable Long orderId,
			@RequestHeader("Authorization") String jwt
	) throws UserException, OrderException, SellerException {
		User user=userService.findUserProfileByJwt(jwt);
		Order order=orderService.cancelOrder(orderId,user);

		Seller seller= sellerService.getSellerById(order.getSellerId());
		SellerReport report=sellerReportService.getSellerReport(seller);

		report.setCanceledOrders(report.getCanceledOrders()+1);
		report.setTotalRefunds(report.getTotalRefunds()+order.getTotalSellingPrice());
		sellerReportService.updateSellerReport(report);

		return ResponseEntity.ok(order);
	}

    private boolean canAccessOrder(User user, Order order) {
        if (user.getId().equals(order.getUser().getId())) {
            return true;
        }
        return isRole("ROLE_ADMIN");
    }

    private boolean isRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
    }

}

