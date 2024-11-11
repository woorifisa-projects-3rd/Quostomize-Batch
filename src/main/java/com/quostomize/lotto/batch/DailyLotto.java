package com.quostomize.lotto.batch;

import com.quostomize.lotto.entity.DailyLottoParticipant;
import com.quostomize.lotto.entity.DailyLottoWinner;
import com.quostomize.lotto.repository.DailyLottoParticipantRepository;
import com.quostomize.lotto.repository.DailyLottoWinnerRepository;
import com.quostomize.lotto.repository.LottoWinnerRecordRepository;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
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

    private long totalParticipants;
    private long restParticipants;
    private int randomIndex;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final DailyLottoWinnerRepository dailyLottoWinnerRepository;
    private final DailyLottoParticipantRepository dailyLottoParticipantRepository;
    private final LottoWinnerRecordRepository lottoWinnerRecordRepository;

    private final Random random;

    private int cnt = 0;

    public DailyLotto(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, DailyLottoWinnerRepository dailyLottoWinnerRepository, DailyLottoParticipantRepository dailyLottoParticipantRepository, LottoWinnerRecordRepository lottoWinnerRecordRepository) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.dailyLottoWinnerRepository = dailyLottoWinnerRepository;
        this.dailyLottoParticipantRepository = dailyLottoParticipantRepository;
        this.lottoWinnerRecordRepository = lottoWinnerRecordRepository;
        this.totalParticipants = dailyLottoParticipantRepository.count();
        this.restParticipants = this.totalParticipants;
        this.random = new Random();
        if (this.restParticipants >= 1000) {
            this.randomIndex = this.random.nextInt(1000);
        } else {
            this.randomIndex = this.random.nextInt((int) this.restParticipants);
        }
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
                .listener(
                        new ChunkListener() {
                            @Override
                            public void beforeChunk(ChunkContext context) {
                                ChunkListener.super.beforeChunk(context);
                            }

                            @Override
                            public void afterChunk(ChunkContext context) {
                                ChunkListener.super.afterChunk(context);
                                StepExecution stepExecution = context.getStepContext().getStepExecution();
                                long getItems = stepExecution.getReadCount();
                                System.out.println("전체 참여자 = "+totalParticipants);
                                System.out.println("불러왔던 아이템들 개수 = "+ getItems);
                                restParticipants = totalParticipants - getItems;
                                System.out.println("랜덤인덱스 = "+ randomIndex+ "였음");
                                System.out.println("현재 restParticipant = " +restParticipants  );
                                if (restParticipants >= 1000) {
                                    randomIndex = random.nextInt(1000);
                                } else if (restParticipants == 0) {
                                    restParticipants = totalParticipants;
                                } else {
                                    randomIndex = random.nextInt((int) restParticipants);
                                }
                                System.out.println("다음 랜덤인덱스 = "+ randomIndex+ "임");
                                System.out.println("남은 개수 = " + restParticipants);
                                cnt = 0;
                            }

                            @Override
                            public void afterChunkError(ChunkContext context) {
                                ChunkListener.super.afterChunkError(context);
                            }
                        }
                )
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

            @Override
            public DailyLottoWinner process(DailyLottoParticipant item) throws Exception {
                cnt ++;
                if (cnt == randomIndex) {
                    return DailyLottoWinner.fromParticipant(item);
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
