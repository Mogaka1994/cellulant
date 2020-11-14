package com.moha.backend.chama.service;

import com.moha.backend.chama.exception.NonRollbackException;
import org.springframework.stereotype.Component;
/**
 *
 * @author moha
 */
@Component
public interface CustomerService {

    public String changeCustomerPin(String msisdn, String pin, String newpin);


    public String AuthenticateCustomer(String msisdn, String pin);
    
    public String processSavingsDeposit(String msisdn, String amount, String ref) throws NonRollbackException; 
}
