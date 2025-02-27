package com.fighter.controller;

import com.fighter.model.Customer;
import com.fighter.model.Loans;
import com.fighter.repository.CustomerRepository;
import com.fighter.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class LoansController {

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;

    @GetMapping("/myLoans")
    @PostAuthorize("hasRole('ADMIN')")
    public List<Loans> getLoanDetails(@RequestParam String email) {
        Optional<Customer> customer = customerRepository.findByEmail(email);
        if (customer.isPresent()){
            List<Loans> loans = loanRepository.findByCustomerIdOrderByStartDtDesc(customer.get().getId());

            if (loans != null)
                return loans;
        }

            return null;

    }

}