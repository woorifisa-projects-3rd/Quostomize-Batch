package com.quostomize.lotto.repository;

import com.quostomize.lotto.entity.DailyLottoParticipant;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DailyLottoParticipantRepository extends JpaRepository<DailyLottoParticipant, Long> {
    @Transactional
    @Modifying
    @Query(value = "TRUNCATE daily_lotto_participant", nativeQuery = true)
    void truncateDailyLottoParticipant();
}
