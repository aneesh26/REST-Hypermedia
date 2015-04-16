package com.grades.appeal.activities;

import com.grades.appeal.model.Identifier;
import com.grades.appeal.model.OrderStatus;
import com.grades.appeal.model.Payment;
import com.grades.appeal.repositories.OrderRepository;
import com.grades.appeal.repositories.PaymentRepository;
import com.grades.appeal.representations.Link;
import com.grades.appeal.representations.PaymentRepresentation;
import com.grades.appeal.representations.Representation;
import com.grades.appeal.representations.RestbucksUri;

public class PaymentActivity {
    public PaymentRepresentation pay(Payment payment, RestbucksUri paymentUri) {
        Identifier identifier = paymentUri.getId();
        
        // Don't know the order!
        if(!OrderRepository.current().has(identifier)) {
            throw new NoSuchOrderException();
        }
        
        // Already paid
        if(PaymentRepository.current().has(identifier)) {
            throw new UpdateException();
        }
        
        // Business rules - if the payment amount doesn't match the amount outstanding, then reject
        if(OrderRepository.current().get(identifier).calculateCost() != payment.getAmount()) {
            throw new InvalidPaymentException();
        }
        
        // If we get here, let's create the payment and update the order status
        OrderRepository.current().get(identifier).setStatus(OrderStatus.PREPARING);
        PaymentRepository.current().store(identifier, payment);
        
        return new PaymentRepresentation(payment, new Link(Representation.RELATIONS_URI + "order", UriExchange.orderForPayment(paymentUri)),
                new Link(Representation.RELATIONS_URI + "receipt", UriExchange.receiptForPayment(paymentUri)));
    }
}
