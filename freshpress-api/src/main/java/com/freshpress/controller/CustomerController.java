package com.freshpress.controller;

import com.freshpress.dto.CreateCustomerRequest;
import com.freshpress.model.Customer;
import com.freshpress.repository.CustomerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;

    @GetMapping
    public Page<Customer> getAllCustomers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return customerRepository.findByNameContainingIgnoreCase(search, pageable);
        }
        return customerRepository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = Customer.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .build();
        return ResponseEntity.ok(customerRepository.save(customer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id,
                                                    @Valid @RequestBody CreateCustomerRequest request) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customer.setName(request.getName());
                    customer.setPhone(request.getPhone());
                    customer.setEmail(request.getEmail());
                    customer.setAddress(request.getAddress());
                    return ResponseEntity.ok(customerRepository.save(customer));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
