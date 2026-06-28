package com.bountygrid.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class RecoveryStory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Alert alert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User author;

    private String title;

    @Column(length = 3000)
    private String story;

    private Integer recoveryTimeHours;

    @Builder.Default
    private Boolean publicStory = true;

    @Builder.Default
    private Integer hearts = 0;

    @Builder.Default
    private Integer claps = 0;

    @CreatedDate
    private LocalDateTime createdAt;
}
