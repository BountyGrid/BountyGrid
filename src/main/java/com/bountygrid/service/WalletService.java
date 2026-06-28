package com.bountygrid.service;

import com.bountygrid.entity.Transaction;
import com.bountygrid.entity.Transaction.TransactionType;
import com.bountygrid.entity.User;
import com.bountygrid.exception.InsufficientBalanceException;
import com.bountygrid.repository.TransactionRepository;
import com.bountygrid.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public List<Transaction> getTransactions(User user) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public void deposit(User user, double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        user.setWalletBalance(user.getWalletBalance() + amount);
        transactionRepository.save(Transaction.builder()
                .user(user).type(TransactionType.DEPOSIT).amount(amount).description("Wallet deposit").build());
        userRepository.save(user);
    }

    @Transactional
    public void withdraw(User user, double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (user.getWalletBalance() < amount) throw new InsufficientBalanceException();
        user.setWalletBalance(user.getWalletBalance() - amount);
        transactionRepository.save(Transaction.builder()
                .user(user).type(TransactionType.WITHDRAWAL).amount(-amount).description("Wallet withdrawal").build());
        userRepository.save(user);
    }
}
