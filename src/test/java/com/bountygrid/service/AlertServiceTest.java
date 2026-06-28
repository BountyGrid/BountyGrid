package com.bountygrid.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.bountygrid.dto.AlertRequest;
import com.bountygrid.entity.Alert;
import com.bountygrid.entity.Alert.AlertCategory;
import com.bountygrid.entity.Alert.AlertType;
import com.bountygrid.entity.User;
import com.bountygrid.repository.AlertRepository;
import com.bountygrid.repository.TipRepository;
import com.bountygrid.repository.TransactionRepository;
import com.bountygrid.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {
    @Mock
    private AlertRepository alertRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TipRepository tipRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AlertService alertService;

    @Test
    void createShouldLockEscrowWhenRewardProvided() {
        User owner = User.builder().name("Ganesh").email("g@example.com").password("secret").walletBalance(500.0).build();
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Alert alert = alertService.create(owner, new AlertRequest(
                "Lost dog",
                "Brown collar",
                AlertType.LOST,
                AlertCategory.PET,
                19.07,
                72.88,
                "Mumbai",
                5.0,
                200.0), null);

        assertThat(owner.getWalletBalance()).isEqualTo(300.0);
        assertThat(alert.getEscrowStatus()).isEqualTo(Alert.EscrowStatus.LOCKED);
        assertThat(alert.getRewardAmount()).isEqualTo(200.0);
    }
}
