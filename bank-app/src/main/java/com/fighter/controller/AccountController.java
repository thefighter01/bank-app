package com.fighter.controller;

import com.fighter.model.Accounts;
import com.fighter.model.Customer;
import com.fighter.repository.AccountsRepository;
import com.fighter.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountsRepository accountsRepository;
    private final CustomerRepository customerRepository;

    @GetMapping("/myAccount")
    public Accounts getAccountDetails(@RequestParam String email) {

        Optional<Customer> customer = customerRepository.findByEmail(email);

        if (customer.isPresent()){
            Accounts accounts = accountsRepository.findByCustomerId(customer.get().getId());
            if (accounts != null) {
                return accounts;
            } else {
                return null;
            }
        }
        return null;

    }

}