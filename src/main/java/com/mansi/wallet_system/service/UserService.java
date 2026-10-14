package com.mansi.wallet_system.service;

import com.mansi.wallet_system.entity.User;
import com.mansi.wallet_system.entity.Wallet;
import com.mansi.wallet_system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       WalletService walletService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
    }

    @Transactional
    public User saveUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }

        User savedUser = userRepository.save(user);

        // Automatically create a wallet for the new user
        Wallet newWallet = new Wallet();
        newWallet.setUserId(savedUser.getId());
        newWallet.setOwnerName(savedUser.getName());
        newWallet.setBalance(BigDecimal.ZERO);
        walletService.createWallet(newWallet);

        return savedUser;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
