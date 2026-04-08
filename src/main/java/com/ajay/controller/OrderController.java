package com.ajay.controller;

import com.ajay.domain.PaymentMethod;
import com.ajay.exception.OrderException;
import com.ajay.exception.SellerException;
import com.ajay.exception.UserException;
import com.ajay.model.*;
import com.ajay.repository.PaymentOrderRepository;
import com.ajay.response.PaymentLinkResponse;
import com.ajay.service.*;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

		PaymentLinkResponse res = new PaymentLinkResponse();

		if(paymentMethod.equals(PaymentMethod.RAZORPAY)){
			PaymentLink payment=paymentService.createRazorpayPaymentLink(user,
					paymentOrder.getAmount(),
					paymentOrder.getId());
			String paymentUrl=payment.get("short_url");
			String paymentUrlId=payment.get("id");


			res.setPayment_link_url(paymentUrl);
//			res.setPayment_link_id(paymentUrlId);
			paymentOrder.setPaymentLinkId(paymentUrlId);
			paymentOrderRepository.save(paymentOrder);
		}
		
		return new ResponseEntity<>(res,HttpStatus.OK);

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
