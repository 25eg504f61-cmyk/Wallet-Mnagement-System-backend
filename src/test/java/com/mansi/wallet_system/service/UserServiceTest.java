package com.mansi.wallet_system.service;

import com.mansi.wallet_system.entity.User;
import com.mansi.wallet_system.entity.Wallet;
import com.mansi.wallet_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private UserService userService;

    @Test
    void testSaveUser_Success() {
        User user = new User();
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setPassword("rawPassword");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Alice");
        savedUser.setEmail("alice@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole("USER");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.saveUser(user);

        assertNotNull(result);
        assertEquals("USER", result.getRole());
        assertEquals("encodedPassword", user.getPassword());
        verify(userRepository, times(1)).save(user);
        verify(walletService, times(1)).createWallet(any(Wallet.class));
    }

    @Test
    void testSaveUser_DuplicateEmail_ThrowsException() {
        User user = new User();
        user.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.saveUser(user));
        verify(userRepository, never()).save(any());
        verify(walletService, never()).createWallet(any());
    }

    @Test
    void testGetAllUsers() {
        User u1 = new User();
        u1.setName("Alice");
        when(userRepository.findAll()).thenReturn(List.of(u1));

        List<User> list = userService.getAllUsers();
        assertEquals(1, list.size());
        assertEquals("Alice", list.get(0).getName());
    }
}
