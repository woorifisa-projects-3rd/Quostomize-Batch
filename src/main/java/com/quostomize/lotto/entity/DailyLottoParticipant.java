package com.quostomize.lotto.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(name="daily_lotto_participant")
public class DailyLottoParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="daily_lotto_participant_id")
    private Long dailyLottoApplicationRecordId;

    @Column(name="customer_id")
    private final Long customerId;

    @Builder
    public DailyLottoParticipant(Long dailyLottoApplicationRecordId, Long customerId) {
        this.dailyLottoApplicationRecordId = dailyLottoApplicationRecordId;
        this.customerId = customerId;
    }

}
