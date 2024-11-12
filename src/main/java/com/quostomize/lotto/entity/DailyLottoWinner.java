package com.quostomize.lotto.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZoneId;

@Entity
@Getter
@Table(name = "daily_lotto_winners")
@NoArgsConstructor
public class DailyLottoWinner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="daily_lotto_winner_id")
    private Long LottoWinnerId;

    @Column(name="lotto_date")
    private LocalDate lottoDate;

    @Column(name="customer_id")
    private Long customerId;


    private DailyLottoWinner(Long customerId) {
        this.customerId = customerId;
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        this.lottoDate = LocalDate.now(zoneId);
    }


    @Builder
    public static DailyLottoWinner fromParticipant(DailyLottoParticipant dailyLottoParticipant) {
        return new DailyLottoWinner(dailyLottoParticipant.getDailyLottoApplicationRecordId());
    }
}
