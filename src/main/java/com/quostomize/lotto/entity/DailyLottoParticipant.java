package com.quostomize.lotto.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name="daily_lotto_participant")
public class DailyLottoParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="daily_lotto_participant_id")
    private Long lottoApplicationRecordId;

    @Column(name="customer_id")
    private Long customerId;

}
