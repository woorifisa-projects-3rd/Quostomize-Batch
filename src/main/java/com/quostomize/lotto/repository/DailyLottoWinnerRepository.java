package com.quostomize.lotto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quostomize.lotto.entity.DailyLottoWinner;

public interface DailyLottoWinnerRepository extends JpaRepository<DailyLottoWinner, Long> {
}
