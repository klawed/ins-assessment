package com.insurance.billing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PolicyNotFoundException extends RuntimeException {
    public PolicyNotFoundException(String policyId) {
        super("Policy not found with id: " + policyId);
    }
}