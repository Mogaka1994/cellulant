package com.moha.backend.chama.service;

import com.moha.backend.chama.exception.NonRollbackException;
import org.springframework.stereotype.Component;
/**
 *
 * @author moha
 */
@Component
public interface CustomerService {

    String changeCustomerPin(String msisdn, String pin, String newpin);

    String AuthenticateCustomer(String msisdn, String pin);
    
    String processSavingsDeposit(String msisdn, String amount, String ref) throws NonRollbackException;
    
    public boolean checkRegistrationstatus(String msisdn);
}
