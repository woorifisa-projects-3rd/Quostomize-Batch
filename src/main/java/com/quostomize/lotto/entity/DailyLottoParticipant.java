package com.quostomize.lotto.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="daily_lotto_participant")
@NoArgsConstructor
public class DailyLottoParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="daily_lotto_participant_id")
    private Long dailyLottoApplicationRecordId;

    @Column(name="customer_id")
    private Long customerId;

    @Builder
    public DailyLottoParticipant(Long dailyLottoApplicationRecordId, Long customerId) {
        this.dailyLottoApplicationRecordId = dailyLottoApplicationRecordId;
        this.customerId = customerId;
    }

}
