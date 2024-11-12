package com.quostomize.lotto.listener;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Component
@Getter
@Slf4j
public class DailyWinnerRecordListener implements JobExecutionListener {

    private final JobLauncher jobLauncher;

    private final Job nextJob;

    public DailyWinnerRecordListener(Job nextJob, JobLauncher jobLauncher) {
        this.nextJob = nextJob;
        this.jobLauncher = jobLauncher;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // 배치 시작 전에 수행할 작업이 있다면 여기에 작성
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("isUnsucessful = " + jobExecution.getStatus().isUnsuccessful() );
        // 첫 번째 배치 작업이 성공적으로 끝난 후 두 번째 배치 작업을 실행
        if (jobExecution.getStatus().isUnsuccessful()) {
            // 실패한 경우 다른 배치를 실행할 필요 없을 수 있음
            return;
        }

        try {
            // 두 번째 배치 실행
            jobLauncher.run(nextJob, jobExecution.getJobParameters());
        } catch (Exception e) {
            log.error(String.valueOf(e));
        }
    }
}
