package com.fighter.config;

import com.fighter.model.Authority;
import com.fighter.repository.CustomerRepository;
import com.fighter.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Profile("!prod")
@RequiredArgsConstructor
public class BankUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      Customer customer =  customerRepo.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User details not found for the user " + username));
       List<GrantedAuthority> authorities = getAuthorities(customer.getAuthorities());

      return new User(customer.getEmail() , customer.getPwd() , authorities);
    }

    private List<GrantedAuthority> getAuthorities(Set<Authority> authorities){

        return authorities.stream().map(authority ->
                new SimpleGrantedAuthority(authority.getName())).collect(Collectors.toList());
    }
}
