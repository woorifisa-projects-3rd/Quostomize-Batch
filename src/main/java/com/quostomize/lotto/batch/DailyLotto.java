package com.quostomize.lotto.batch;

import com.quostomize.lotto.entity.DailyLottoParticipant;
import com.quostomize.lotto.entity.DailyLottoWinner;
import com.quostomize.lotto.repository.DailyLottoParticipantRepository;
import com.quostomize.lotto.repository.DailyLottoWinnerRepository;
import com.quostomize.lotto.repository.LottoWinnerRecordRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Random;

@Configuration
public class DailyLotto {

    private final Long totalParticipants;
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
        this.totalParticipants = dailyLottoParticipantRepository.count();
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
                .processor(drawingProcessor())
                .writer(winnerWriter())
                .build();

    }

    @Bean
    public RepositoryItemReader<DailyLottoParticipant> participantReader() {

        return new RepositoryItemReaderBuilder<DailyLottoParticipant>()
                .name("participantReader")
                .pageSize(1000)
                .methodName("findAll")
                .repository(dailyLottoParticipantRepository)
                .sorts(new HashMap<>())
                .build();

    }

    @Bean
    public ItemProcessor<DailyLottoParticipant, DailyLottoWinner> drawingProcessor() {

        return new ItemProcessor<DailyLottoParticipant, DailyLottoWinner>() {
            private int cnt = 0;
            private final Random random = new Random();
            private DailyLottoParticipant selectedParticipant = null;

            @BeforeChunk
            public void beforeChunk(ChunkContext context) {

            }

            @Override
            public DailyLottoWinner process(DailyLottoParticipant item) throws Exception {
                cnt ++;

                if (cnt == 1) {
                    selectedParticipant = item;
                } else {
                    if (random.nextInt(cnt) == 0) {
                        selectedParticipant = item;
                    }
                }

                if (cnt == 1000) {
                    cnt = 0;
                }

                return null;
            }
        };
    }

    @Bean
    public RepositoryItemWriter<DailyLottoWinner> winnerWriter() {

        return new RepositoryItemWriterBuilder<DailyLottoWinner>()
                .repository(dailyLottoWinnerRepository)
                .methodName("save")
                .build();
    }

}
