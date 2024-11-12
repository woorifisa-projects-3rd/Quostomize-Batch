package com.quostomize.lotto.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "lotto_winner_records")
@NoArgsConstructor
public class LottoWinnerRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="lotto_winner_id")
    private Long lottoWinnerRecordId;

    @Column(name="lotto_date")
    private LocalDate lottoDate;

    @Column(name="customer_id")
    private Long customerId;

    private LottoWinnerRecord(LocalDate lottoDate, Long customerId) {
        this.lottoDate = lottoDate;
        this.customerId = customerId;
    }

    public static LottoWinnerRecord fromDailyWinner(DailyLottoWinner dailyLottoWinner) {
        return new LottoWinnerRecord(dailyLottoWinner.getLottoDate(), dailyLottoWinner.getCustomerId());
    }
}
