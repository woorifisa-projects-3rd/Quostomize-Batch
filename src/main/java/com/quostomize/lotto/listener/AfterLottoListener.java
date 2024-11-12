package com.quostomize.lotto.listener;

import com.quostomize.lotto.repository.DailyLottoParticipantRepository;
import com.quostomize.lotto.repository.DailyLottoWinnerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

@Slf4j
public class AfterLottoListener implements JobExecutionListener {

    private final DailyLottoParticipantRepository dailyLottoParticipantRepository;


    public  AfterLottoListener(DailyLottoParticipantRepository dailyLottoParticipantRepository) {
        this.dailyLottoParticipantRepository = dailyLottoParticipantRepository;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // 배치 시작 전에 수행할 작업이 있다면 여기에 작성
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        // 첫 번째 배치 작업이 성공적으로 끝난 후 두 번째 배치 작업을 실행
        if (jobExecution.getStatus().isUnsuccessful()) {
            // 실패한 경우 다른 배치를 실행할 필요 없을 수 있음
            return;
        }
        try {
            // 실제 운영 시에는 제거
//            dailyLottoParticipantRepository.truncateDailyLottoWinner();
        } catch (Exception e) {
            log.error(String.valueOf(e));
        }
    }
}
