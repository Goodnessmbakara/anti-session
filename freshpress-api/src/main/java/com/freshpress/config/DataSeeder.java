package com.freshpress.config;

import com.freshpress.model.*;
import com.freshpress.repository.CustomerRepository;
import com.freshpress.repository.OrderRepository;
import com.freshpress.repository.ServiceItemRepository;
import com.freshpress.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        log.info("🧺 Seeding FreshPress demo data...");

        // Create admin user
        User admin = User.builder()
                .fullName("Goodness Mbakara")
                .email("admin@freshpress.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);

        // Create service items (pricing catalog)
        ServiceItem washFold = serviceItemRepository.save(ServiceItem.builder()
                .name("Wash & Fold").category(ServiceCategory.WASH).pricePerUnit(new BigDecimal("1500")).unitType("KG").build());
        ServiceItem dryClean = serviceItemRepository.save(ServiceItem.builder()
                .name("Dry Cleaning").category(ServiceCategory.DRY_CLEAN).pricePerUnit(new BigDecimal("3000")).unitType("PIECE").build());
        ServiceItem ironOnly = serviceItemRepository.save(ServiceItem.builder()
                .name("Iron Only").category(ServiceCategory.IRON).pricePerUnit(new BigDecimal("500")).unitType("PIECE").build());
        ServiceItem washIron = serviceItemRepository.save(ServiceItem.builder()
                .name("Wash & Iron").category(ServiceCategory.WASH_AND_IRON).pricePerUnit(new BigDecimal("2000")).unitType("KG").build());
        ServiceItem specialCare = serviceItemRepository.save(ServiceItem.builder()
                .name("Special Care (Delicates)").category(ServiceCategory.SPECIAL_CARE).pricePerUnit(new BigDecimal("5000")).unitType("PIECE").build());

        // Create customers
        Customer customer1 = customerRepository.save(Customer.builder()
                .name("Amara Okafor").phone("+2348012345678").email("amara@email.com").address("12 Aba Road, Uyo").build());
        Customer customer2 = customerRepository.save(Customer.builder()
                .name("Emeka Nwosu").phone("+2348098765432").email("emeka@email.com").address("45 Ikot Ekpene Rd, Uyo").build());
        Customer customer3 = customerRepository.save(Customer.builder()
                .name("Funke Adeyemi").phone("+2349011223344").address("8 Wellington Bassey Way, Uyo").build());

        // Create sample orders
        Order order1 = Order.builder()
                .customer(customer1).status(OrderStatus.PROCESSING)
                .pickupDate(LocalDateTime.now().minusDays(2))
                .deliveryDate(LocalDateTime.now().plusDays(1))
                .notes("Handle with care — silk items included").build();
        order1.getItems().add(OrderItem.builder().order(order1).serviceItem(washFold).quantity(3)
                .subtotal(washFold.getPricePerUnit().multiply(BigDecimal.valueOf(3))).build());
        order1.getItems().add(OrderItem.builder().order(order1).serviceItem(dryClean).quantity(2)
                .subtotal(dryClean.getPricePerUnit().multiply(BigDecimal.valueOf(2))).build());
        order1.recalculateTotal();
        orderRepository.save(order1);

        Order order2 = Order.builder()
                .customer(customer2).status(OrderStatus.PENDING)
                .pickupDate(LocalDateTime.now())
                .deliveryDate(LocalDateTime.now().plusDays(3)).build();
        order2.getItems().add(OrderItem.builder().order(order2).serviceItem(washIron).quantity(5)
                .subtotal(washIron.getPricePerUnit().multiply(BigDecimal.valueOf(5))).build());
        order2.recalculateTotal();
        orderRepository.save(order2);

        Order order3 = Order.builder()
                .customer(customer3).status(OrderStatus.DELIVERED)
                .pickupDate(LocalDateTime.now().minusDays(5))
                .deliveryDate(LocalDateTime.now().minusDays(2)).build();
        order3.getItems().add(OrderItem.builder().order(order3).serviceItem(ironOnly).quantity(10)
                .subtotal(ironOnly.getPricePerUnit().multiply(BigDecimal.valueOf(10))).build());
        order3.recalculateTotal();
        orderRepository.save(order3);

        log.info("✅ FreshPress demo data seeded: 1 admin, 5 services, 3 customers, 3 orders");
    }
}
