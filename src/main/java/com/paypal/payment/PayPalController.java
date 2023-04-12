package com.paypal.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.payment.configuration.PaypalConfiguration;
import com.paypal.payment.order.OrderDetails;
import com.paypal.payment.service.PaypalService;

@Controller
public class PayPalController {
 
	@Autowired
	PaypalService paypalservice;
	
	@Autowired
	PaypalConfiguration paypalConfiguration;

	@GetMapping("/")
	public String home() {
		return "homepage";
	}

	@PostMapping("/payment")
	public String payment(@ModelAttribute("order") OrderDetails orderDetails) {
		try {
			Payment payment = paypalservice.createPayment(orderDetails.getPrice(), orderDetails.getCurrency(), orderDetails.getMethod(),
					orderDetails.getIntent(), orderDetails.getDescription(), "http://localhost:8080/" + "payment/failed",
					"http://localhost:8080/" + "payment/success");
			for(Links link:payment.getLinks()) {
				if(link.getRel().equals("approval_url")) {
					return "redirect:"+link.getHref();
				}
			}
			
		} catch (PayPalRESTException e) {
		
			e.printStackTrace();
		}
		return "redirect:/";
	}
	
	 @GetMapping(value = "payment/failed")
	    public String cancelPay() {
	        return "failed";
	    }

	    @GetMapping(value = "payment/success")
	    public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
	        try {
	            Payment payment = paypalservice.executePayment(paymentId, payerId);
	            System.out.println(payment.toJSON());
	            if (payment.getState().equals("approved")) {
	                return "success";
	            }
	        } catch (PayPalRESTException e) {
	         System.out.println(e.getMessage());
	        }
	        return "redirect:/";
	    }

}
