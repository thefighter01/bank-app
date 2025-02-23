package com.fighter.controller;

import com.fighter.constants.ApplicationConstants;
import com.fighter.model.Customer;
import com.fighter.model.LoginRequestDTO;
import com.fighter.model.LoginResponseDTO;
import com.fighter.repository.CustomerRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final Environment env;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Customer customer) {
        try {
            String hashPwd = passwordEncoder.encode(customer.getPwd());
            customer.setPwd(hashPwd);
            customer.setCreateDt(new Date(System.currentTimeMillis()));
            Customer savedCustomer = customerRepository.save(customer);

            if (savedCustomer.getId() > 0) {
                return ResponseEntity.status(HttpStatus.CREATED).
                        body("Given user details are successfully registered");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                        body("User registration failed");
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("An exception occurred: " + ex.getMessage());
        }
    }

    @RequestMapping("/user")
    public Customer getUserDetailsAfterLogin(Authentication authentication) {
        Optional<Customer> optionalCustomer = customerRepository.findByEmail(authentication.getName());
        return optionalCustomer.orElse(null);
    }

    @PostMapping("/apiLogin")
    public ResponseEntity<LoginResponseDTO> apiLogin(@RequestBody LoginRequestDTO loginRequest) {
        String jwt = "";
        Authentication authentication =  UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.username(), loginRequest.password());
        Authentication authResponse =  authenticationManager.authenticate(authentication);
       if (authResponse != null && authResponse.isAuthenticated()) {
           if (env != null)  {
               String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY , ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
               SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

                jwt = Jwts.builder().issuer("Bank").subject("JWT Token")
                       .claim("username", authResponse.getName()).claim("authorities", authResponse.getAuthorities().stream().
                               map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                       .issuedAt(new java.util.Date())
                       .expiration(new java.util.Date((new java.util.Date()).getTime() + 8 * 60 * 60 * 1000))
                       .signWith(secretKey).compact();


           }

       }

       return ResponseEntity.status(HttpStatus.OK).header(ApplicationConstants.JWT_HEADER, jwt).body(new LoginResponseDTO(HttpStatus.OK.getReasonPhrase(), jwt));
    }

}