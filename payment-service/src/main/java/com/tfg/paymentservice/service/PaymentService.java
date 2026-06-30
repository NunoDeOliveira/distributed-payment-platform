package com.tfg.paymentservice.service;

import com.tfg.paymentservice.message.PaymentPublish;
import com.tfg.paymentservice.model.Payment;
import com.tfg.paymentservice.model.PaymentState;
import com.tfg.paymentservice.repository.PaymentRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
//import org.springframework.scheduling.annotation.Scheduled;


@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentPublish paymentPublish;

    public PaymentService(PaymentRepository paymentRepository,
                             PaymentPublish paymentPublish) {
        this.paymentRepository = paymentRepository;
        this.paymentPublish = paymentPublish;
    }

    // Given an ID and amount of Payments create a Payment
    public Payment createPayment(int amount) {
        Payment newPayment = new Payment(
                    amount, PaymentState.CREATED, LocalDateTime.now());

        Payment storedPayment = paymentRepository.save(newPayment);

        // Publish on RabbitMQ for consume this event
        paymentPublish.publishPaymentCreated(
                        storedPayment.getId(), storedPayment.getAmount());
                        
        // Change payment state to WAITING while waiting
        waitingResponse(storedPayment.getId());

        return storedPayment;
    }
    
    // Change payment state from CREATED to WAITING
    public void waitingResponse(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);        
        if (payment == null || payment.getState() != PaymentState.CREATED) {
            return;
        }
        
        payment.waiting();
        paymentRepository.save(payment);
    }
   
    @Async
    // Given an ID of payment from the RabbitMQ start a new payment
    public void startPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null || payment.getState() != PaymentState.WAITING) {
            return;
        }
        
        payment.start();
        // Update state in database
        paymentRepository.save(payment);

        try{
            // Simulate the payment processing (0,001 seconds)
            Thread.sleep(500);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            return;
        }
        // Apply the logic for a completed payment when de payment finish
        completePayment(paymentId);
    }

    // Method for saving a rejected payment in the DB
    public void rejectPayment(Payment paymentRejected, int amountAllowed) {   
        int originalAmount = paymentRejected.getAmount();
        // Case the stock is completed
        if (amountAllowed == 0) {
            paymentRejected.reject();
            paymentRepository.save(paymentRejected);
            
        // Case needed compensation transaction. Stock is not completed    
        } else if (amountAllowed > 0 && amountAllowed < originalAmount) {
            paymentRejected.reject(); 
            paymentRepository.save(paymentRejected);
            compensateRejectedPayment(paymentRejected, amountAllowed);
            
         // This case should not happen   
        } else {
            System.out.println("WARNING: There is an error with paymentId=" 
                                                + paymentRejected.getId());
        }
    }
    
    // Saga compensating transaction method.
    // Given a rejected payment and the maximum amount allowed for that payment
    public void compensateRejectedPayment(Payment paymentRejected, 
                                                    int maxAllowedAmount) {
        int originalAmount = paymentRejected.getAmount();
        int pendingAmount = originalAmount - maxAllowedAmount;
      
        // Create new protuction with allowd amount
        createPayment(maxAllowedAmount);
        
        System.out.println("Partial compensation: created new payment with amount=" 
                            + maxAllowedAmount);
        // Save the rest of the payment rejected as PENDING
        /*if (pendingAmount > 0) {
            Payment newPending = new Payment(
                        pendingAmount, PaymentState.PENDING, LocalDateTime.now());
            paymentRepository.save(newPending);
        }*/
    }
    
    @Transactional
    // When the payment is completed save payment in repository
    // and publish an event on RabbitMQ
    public void completePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);    
        if (payment == null || payment.getState() == PaymentState.CANCELLED ||
                                  payment.getState() == PaymentState.COMPLETED) {
            return;
        }
        
        payment.complete();
        paymentRepository.save(payment);
        // Method to send event to RabbitMQ
        publishPaymentCompleted(payment.getId(), payment.getAmount());
    }
    
    // Given an Id of a payment publish that payment in the queue
    private void publishPaymentCompleted(Long paymentId, int amount) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) {
            return;
        }
        
        // send event to RabbitMQ
        paymentPublish.publishPaymentCompleted(
                                    payment.getId(), payment.getAmount());
    }
    
    // Get payment by ID
    public Payment getPayment(Long id) {
        Optional<Payment> payment = paymentRepository.findById(id);
        Payment paymentToReturn = payment.orElseThrow(()
                    -> new RuntimeException("Payment " + id + "not found"));

        return paymentToReturn;
    }
    
    // Given an id of payment cancell that payment
    public void cancelPayment(Long id) {
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment == null || 
            payment.getState() == PaymentState.COMPLETED ||
            payment.getState() == PaymentState.CANCELLED) {  
            return;
        } 
        payment.cancelled();
        paymentRepository.save(payment);
    }
    
    // Given an id of payment cancell that payment
    public void cancelPaymentByUser(Long id) {
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment == null || 
            payment.getState() == PaymentState.COMPLETED ||
            payment.getState() == PaymentState.CANCELLED) {  
            return;
        } 
        payment.cancelled();
        paymentRepository.save(payment);
        paymentPublish.publishPaymentCancelled(id, payment.getAmount());
    }
    
    
    

    // Get all the payment from the repository
    // This query is to return to the user
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
        /*
    // Given an rejeted payment manage timeout and fail 
    private void handleRetry(Payment paymentRejected) {
        paymentRejected.incrementRetry();
        
        // Case faill 3 times the state will be failed
        if (paymentRejected.getRetryCount() >= 3) {
            paymentRejected.fail();
            paymentRepository.save(paymentRejected);
            
        // case fail 1 time the state will be pending
        } else {
            paymentRejected.pending();
            paymentRepository.save(paymentRejected);
        }
    }
    
      
    // When a payment is rejected because it excceds the stock 
    // and is assigned as PENDING, this method start this a pending payment
    //@Scheduled(fixedDelay = 10000)
    public void processPendingPayments() {
        Optional<Payment> pending = paymentRepository
                  .findFirstByStateOrderByStartTimeAsc(PaymentState.PENDING);
        if (pending.isPresent()) {
            paymentPublish.publishPaymentPending(
                              pending.get().getId(), pending.get().getAmount());
        }
    }*/
    
    // If inventory connection fail get timeout state
    public void getTimeoutState(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);        
        if (payment == null) {
            return;
        }
        payment.timeout();
        paymentRepository.save(payment);
    }
    
    /*
    // When the third retry fails, the state is failed
    public void getFailedSate(Payment payment) {
        payment.fail();
        paymentRepository.save(payment);
    }*/
    
}
