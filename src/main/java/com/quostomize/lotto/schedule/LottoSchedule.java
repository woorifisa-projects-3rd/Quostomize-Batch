package com.quostomize.lotto.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Configuration
@RequiredArgsConstructor
public class LottoSchedule {
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    // 실제 운영용: 매일 정오에 실행
//    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Seoul")
//    public void runDailyLotto() throws Exception {
//        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addString("date", date)
//                .toJobParameters();
//
//        jobLauncher.run(jobRegistry.getJob("lottoJob"), jobParameters);
//    }

    // 테스트용: 매 분 실
    @Scheduled(cron = "10 * * * * *", zone = "Asia/Seoul")
    public void runTestLotto() throws Exception {
        String executionId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executionId", executionId)
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("locateJob"), jobParameters);
    }
}
