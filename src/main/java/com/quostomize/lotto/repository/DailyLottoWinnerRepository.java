package com.quostomize.lotto.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.quostomize.lotto.entity.DailyLottoWinner;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DailyLottoWinnerRepository extends JpaRepository<DailyLottoWinner, Long> {

    @Transactional
    @Modifying
    @Query(value = "TRUNCATE daily_lotto_winners", nativeQuery = true)
    void truncateDailyLottoWinner();
}
