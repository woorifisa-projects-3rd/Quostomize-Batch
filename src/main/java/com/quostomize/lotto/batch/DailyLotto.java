package com.quostomize.lotto.batch;

import com.quostomize.lotto.entity.DailyLottoParticipant;
import com.quostomize.lotto.entity.DailyLottoWinner;
import com.quostomize.lotto.repository.DailyLottoParticipantRepository;
import com.quostomize.lotto.repository.DailyLottoWinnerRepository;
import com.quostomize.lotto.repository.LottoWinnerRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class DailyLotto {

    private final Long totalParticipant;
    private int current = 0;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final DailyLottoWinnerRepository dailyLottoWinnerRepository;
    private final DailyLottoParticipantRepository dailyLottoParticipantRepository;
    private final LottoWinnerRecordRepository lottoWinnerRecordRepository;

    public DailyLotto(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, DailyLottoWinnerRepository dailyLottoWinnerRepository, DailyLottoParticipantRepository dailyLottoParticipantRepository, LottoWinnerRecordRepository lottoWinnerRecordRepository) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.dailyLottoWinnerRepository = dailyLottoWinnerRepository;
        this.dailyLottoParticipantRepository = dailyLottoParticipantRepository;
        this.lottoWinnerRecordRepository = lottoWinnerRecordRepository;
        this.totalParticipant = dailyLottoParticipantRepository.count();
    }


    @Bean
    public Job lottoJob() {
        return new JobBuilder("lottoJob", jobRepository)
                .start(dailyLottoStep())
                .build();
    }

    @Bean
    public Step dailyLottoStep() {
        return new StepBuilder("lottoStep", jobRepository)
                .<DailyLottoParticipant, DailyLottoWinner> chunk(1000, platformTransactionManager)
                .reader(participantReader())
                .processor()
                .writer()
                .build();

    }

    @Bean
    public RepositoryItemReader<DailyLottoParticipant> participantReader() {

        return new RepositoryItemReaderBuilder<DailyLottoParticipant>()
                .name("participantReader")
                .pageSize(1000)
                .methodName("findAll")
                .repository(dailyLottoParticipantRepository)
                .build();

    }




}
