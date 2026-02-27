package com.freshpress.controller;

import com.freshpress.model.ServiceItem;
import com.freshpress.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceItemController {

    private final ServiceItemRepository serviceItemRepository;

    @GetMapping
    public List<ServiceItem> getAllServices() {
        return serviceItemRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<ServiceItem> createService(@RequestBody ServiceItem serviceItem) {
        return ResponseEntity.ok(serviceItemRepository.save(serviceItem));
    }
}
