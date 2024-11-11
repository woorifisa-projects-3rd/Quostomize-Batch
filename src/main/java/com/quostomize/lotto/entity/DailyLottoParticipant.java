package com.quostomize.lotto.entity;

import jakarta.persistence.*;

@Entity
@Table(name="lotto_application_records")
public class DailyLottoParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="lotto_application_record_id")
    private Long lottoApplicationRecordId;

    @Column(name="customer_id")
    private Long customerId;

}
