package com.quostomize.lotto.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "lotto_winner_records")
public class LottoWinnerRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="lotto_winner_id")
    private Long lottoWinnerRecordId;

    @Column(name="lotto_date")
    private LocalDate lottoDate;

    @Column(name="customer_id")
    private Long customerId;

}
