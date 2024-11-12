package com.quostomize.lotto.batch;

import com.quostomize.lotto.entity.DailyLottoParticipant;
import com.quostomize.lotto.entity.DailyLottoWinner;
import com.quostomize.lotto.entity.LottoWinnerRecord;
import com.quostomize.lotto.listener.AfterLottoListener;
import com.quostomize.lotto.listener.DailyWinnerRecordListener;
import com.quostomize.lotto.repository.DailyLottoParticipantRepository;
import com.quostomize.lotto.repository.DailyLottoWinnerRepository;
import com.quostomize.lotto.repository.LottoWinnerRecordRepository;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
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
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Random;

@Configuration
public class DailyLotto {

    private long totalParticipants;
    private long restParticipants = -1;
    private int randomIndex;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final JobLauncher jobLauncher;

    private final DailyLottoWinnerRepository dailyLottoWinnerRepository;
    private final DailyLottoParticipantRepository dailyLottoParticipantRepository;
    private final LottoWinnerRecordRepository lottoWinnerRecordRepository;

    private final Random random;

    private int cnt = 0;

    public DailyLotto(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, DailyLottoWinnerRepository dailyLottoWinnerRepository, DailyLottoParticipantRepository dailyLottoParticipantRepository, LottoWinnerRecordRepository lottoWinnerRecordRepository, JobLauncher jobLauncher) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.dailyLottoWinnerRepository = dailyLottoWinnerRepository;
        this.dailyLottoParticipantRepository = dailyLottoParticipantRepository;
        this.lottoWinnerRecordRepository = lottoWinnerRecordRepository;
        this.jobLauncher = jobLauncher;
        this.random = new Random();
    }

    /*

    기존에 있던 오늘 당첨자를 당첨자 기록 테이블로 옮기기

     */

    @Primary
    @Bean
    public Job locateJob() {
        return new JobBuilder("locateJob", jobRepository)
                .start(dailyWinnerlocateStep())
                .listener(new DailyWinnerRecordListener(lottoJob(), jobLauncher, dailyLottoWinnerRepository))
                .build();
    }

    @Primary
    @Bean
    public Step dailyWinnerlocateStep() {
        return new StepBuilder("locateStep", jobRepository)
                .<DailyLottoWinner, LottoWinnerRecord> chunk(100, platformTransactionManager)
                .reader(dailyWinnerReader())
                .processor(winnerRecordProcessor())
                .writer(winnerRecordWriter())
                .build();
    }

    @Primary
    @Bean
    public RepositoryItemReader<DailyLottoWinner> dailyWinnerReader() {

        return new RepositoryItemReaderBuilder<DailyLottoWinner>()
                .name("dailyWinnerReader")
                .pageSize(100)
                .methodName("findAll")
                .repository(dailyLottoWinnerRepository)
                .sorts(new HashMap<>())
                .build();
    }

    @Primary
    @Bean
    public ItemProcessor<DailyLottoWinner, LottoWinnerRecord> winnerRecordProcessor() {
        return new ItemProcessor<DailyLottoWinner, LottoWinnerRecord>() {

            @Override
            public LottoWinnerRecord process(DailyLottoWinner item) throws Exception {
                return LottoWinnerRecord.fromDailyWinner(item);
            }
        };
    }

    @Primary
    @Bean
    public RepositoryItemWriter<LottoWinnerRecord> winnerRecordWriter() {
        return new RepositoryItemWriterBuilder<LottoWinnerRecord>()
                .repository(lottoWinnerRecordRepository)
                .methodName("save")
                .build();
    }


    /*

    오늘 참여자에서 당첨자들 추첨

     */



    @Bean
    public Job lottoJob() {
        return new JobBuilder("lottoJob", jobRepository)
                .start(dailyLottoStep())
                .listener(new AfterLottoListener(dailyLottoParticipantRepository))
                .build();
    }

    @Bean
    public Step dailyLottoStep() {
        return new StepBuilder("lottoStep", jobRepository)
                .<DailyLottoParticipant, DailyLottoWinner> chunk(1000, platformTransactionManager)
                .reader(participantReader())
                .processor(drawingProcessor())
                .writer(winnerWriter())
                // chunkListener를 통해 남은 아이템 계산
                .listener(
                        new ChunkListener() {
                            @Override
                            public void beforeChunk(ChunkContext context) {
                                if (restParticipants == -1) {
                                    totalParticipants = dailyLottoParticipantRepository.count();
                                    restParticipants = totalParticipants;
                                    randomIndex = (restParticipants >= 1000)
                                            ? random.nextInt(1000)
                                            : random.nextInt((int) restParticipants);
                                } else if (restParticipants != 0) {
                                    randomIndex = (restParticipants >= 1000)
                                            ? random.nextInt(1000)
                                            : random.nextInt((int) restParticipants);
                                }
                                ChunkListener.super.beforeChunk(context);
                            }

                            @Override
                            public void afterChunk(ChunkContext context) {
                                ChunkListener.super.afterChunk(context);
                                StepExecution stepExecution = context.getStepContext().getStepExecution();
                                long getItems = stepExecution.getReadCount();
                                restParticipants = totalParticipants - getItems;
                                if (restParticipants >= 1000L) {
                                    randomIndex = random.nextInt(1000)+1;
                                }  else if (restParticipants > 0L) {
                                    randomIndex = random.nextInt((int) restParticipants)+1;
                                }
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
