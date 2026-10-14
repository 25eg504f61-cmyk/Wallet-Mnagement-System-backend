package com.mansi.wallet_system.config;

import com.mansi.wallet_system.entity.User;
import com.mansi.wallet_system.repository.UserRepository;
import com.mansi.wallet_system.repository.WalletRepository;
import com.mansi.wallet_system.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = false)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final UserService userService;
    private final WalletRepository walletRepository;

    public DataInitializer(UserRepository userRepository,
                           UserService userService,
                           WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.walletRepository = walletRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("No users found. Seeding initial test accounts...");

            // 1. Admin Account
            User admin = new User("Admin", "admin@wallet.com", "Admin@123", BigDecimal.valueOf(5000), "ADMIN");
            User savedAdmin = userService.saveUser(admin);
            walletRepository.findByUserId(savedAdmin.getId()).ifPresent(w -> {
                w.setBalance(BigDecimal.valueOf(5000));
                walletRepository.save(w);
            });

            // 2. Demo User Account
            User user = new User("John Doe", "user@wallet.com", "Password@123", BigDecimal.valueOf(1000), "USER");
            User savedUser = userService.saveUser(user);
            walletRepository.findByUserId(savedUser.getId()).ifPresent(w -> {
                w.setBalance(BigDecimal.valueOf(1000));
                walletRepository.save(w);
            });

            // 3. Demo Recipient Account
            User alex = new User("Alex Smith", "alex@wallet.com", "Password@123", BigDecimal.valueOf(500), "USER");
            User savedAlex = userService.saveUser(alex);
            walletRepository.findByUserId(savedAlex.getId()).ifPresent(w -> {
                w.setBalance(BigDecimal.valueOf(500));
                walletRepository.save(w);
            });

            log.info("Sample accounts seeded successfully:");
            log.info(" - admin@wallet.com / Admin@123 (ADMIN)");
            log.info(" - user@wallet.com / Password@123 (USER)");
            log.info(" - alex@wallet.com / Password@123 (USER)");
        }
    }
}
