package com.smartexpense.config;

import com.smartexpense.entity.Category;
import com.smartexpense.entity.PaymentMethod;
import com.smartexpense.entity.User;
import com.smartexpense.repository.CategoryRepository;
import com.smartexpense.repository.PaymentMethodRepository;
import com.smartexpense.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Food", "Transport", "Shopping", "Entertainment", "Bills",
            "Medical", "Education", "Travel", "Others"
    );

    private static final List<String> DEFAULT_PAYMENT_METHODS = List.of(
            "Cash", "UPI", "Credit Card", "Debit Card", "Net Banking", "Wallet"
    );

    private final CategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CategoryRepository categoryRepository,
                           PaymentMethodRepository paymentMethodRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Starting database initialization (seed data)...");
        seedCategories();
        seedPaymentMethods();
        seedDefaultUser();
        log.info("Database initialization completed.");
    }

    private void seedDefaultUser() {
        seedUser("admin", "admin@smartexpense.local", "admin123");
        // Demo users to showcase per-user expense isolation
        seedUser("yash", "yash@smartexpense.local", "password123");
        seedUser("om", "om@smartexpense.local", "password123");
    }

    private void seedUser(String username, String email, String rawPassword) {
        if (!userRepository.existsByUsernameIgnoreCase(username)) {
            userRepository.save(User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .enabled(true)
                    .build());
            log.info("Inserted user: {} (change the password after first login)", username);
        }
    }

    private void seedCategories() {
        for (String name : DEFAULT_CATEGORIES) {
            if (!categoryRepository.existsByNameIgnoreCase(name)) {
                categoryRepository.save(Category.builder()
                        .name(name)
                        .description("Default category: " + name)
                        .build());
                log.info("Inserted default category: {}", name);
            }
        }
    }

    private void seedPaymentMethods() {
        for (String name : DEFAULT_PAYMENT_METHODS) {
            if (!paymentMethodRepository.existsByNameIgnoreCase(name)) {
                paymentMethodRepository.save(PaymentMethod.builder()
                        .name(name)
                        .description("Default payment method: " + name)
                        .build());
                log.info("Inserted default payment method: {}", name);
            }
        }
    }
}
