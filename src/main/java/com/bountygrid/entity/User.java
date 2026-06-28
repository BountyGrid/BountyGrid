package com.bountygrid.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    private String phone;
    private String city;
    private String avatarUrl;

    @Builder.Default
    private Double walletBalance = 0.0;

    @Builder.Default
    private Integer points = 0;

    @Builder.Default
    private Integer finds = 0;

    @Builder.Default
    private String currentBadge = "newcomer";

    @Builder.Default
    private Integer sosUsedThisMonth = 0;

    @Builder.Default
    private Boolean leaderOfMonth = false;

    @Builder.Default
    private String role = "ROLE_USER";

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void addPoints(int amount) {
        points += amount;
        currentBadge = com.bountygrid.util.BadgeEvaluator.evaluate(points, finds);
    }
}
