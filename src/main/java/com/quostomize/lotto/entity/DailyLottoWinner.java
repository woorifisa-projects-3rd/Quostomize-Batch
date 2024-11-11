package com.quostomize.lotto.entity;


import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_lotto_winners")
public class DailyLottoWinner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="daily_lotto_winner_id")
    private Long lotto_winner_id;

    @Column(name="lotto_date")
    private LocalDate lottoDate;

    @Column(name="customer_id")
    private Long customerId;

}
