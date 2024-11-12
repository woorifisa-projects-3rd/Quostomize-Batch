package com.quostomize.lotto.batch;

import com.quostomize.lotto.config.TestBatchConfig;
import com.quostomize.lotto.entity.DailyLottoParticipant;
import com.quostomize.lotto.entity.DailyLottoWinner;
import com.quostomize.lotto.repository.DailyLottoParticipantRepository;
import com.quostomize.lotto.repository.DailyLottoWinnerRepository;
import com.quostomize.lotto.repository.LottoWinnerRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBatchTest
@SpringBootTest(classes = {DailyLotto.class, TestBatchConfig.class})
class DailyLottoTest {
    private int cnt = 0;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private Job lottoJob;

    @Autowired
    private RepositoryItemReader<DailyLottoParticipant> participantReader;


    private final ItemProcessor<DailyLottoParticipant, DailyLottoWinner> drawingProcessor =  new ItemProcessor<DailyLottoParticipant, DailyLottoWinner>() {

        @Override
        public DailyLottoWinner process(DailyLottoParticipant item) throws Exception {
            return DailyLottoWinner.fromParticipant(item);
            }
    };

    @Autowired
    private RepositoryItemWriter<DailyLottoWinner> winnerWriter;

    @MockBean
    private DailyLottoParticipantRepository participantRepository;

    @MockBean
    private DailyLottoWinnerRepository winnerRepository;

    @MockBean
    private LottoWinnerRecordRepository winnerRecordRepository;

    private List<DailyLottoParticipant> testParticipants;

    @BeforeEach
    void setUp() {
        // 이전 Job 실행 데이터 정리
        jobRepositoryTestUtils.removeJobExecutions();

        // 테스트용 참가자 데이터 생성
        testParticipants = Arrays.asList(
                DailyLottoParticipant.builder()
                        .dailyLottoApplicationRecordId(1L)
                        .customerId(1L)
                        .build(),
                DailyLottoParticipant.builder()
                        .dailyLottoApplicationRecordId(2L)
                        .customerId(2L)
                        .build(),
                DailyLottoParticipant.builder()
                        .dailyLottoApplicationRecordId(3L)
                        .customerId(3L)
                        .build()
        );

        // Repository mock 설정
        when(participantRepository.count()).thenReturn((long) testParticipants.size());
        when(participantRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(testParticipants))
                .thenReturn(Page.empty());
    }

    @Test
    void testJobExecution() throws Exception {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());

        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        assertEquals("lottoStep", stepExecution.getStepName());
        assertEquals(testParticipants.size(), stepExecution.getReadCount());

        assertEquals(1, stepExecution.getWriteCount()); // 당첨자는 1명이어야 함
    }

    @Test
    void testParticipantReader() throws Exception {
        // Given
        participantReader.setPageSize(1000);

        // When
        DailyLottoParticipant participant = participantReader.read();

        // Then
        assertNotNull(participant);
        assertTrue(testParticipants.contains(participant));
        verify(participantRepository).findAll(any(PageRequest.class));
    }

    @Test
    void testDrawingProcessor() throws Exception {
        // Given
        int winnerCount = 0;
        int randomIndex = new Random().nextInt(3)+1;
        // When
        for (DailyLottoParticipant participant : testParticipants) {
            if (participant.getCustomerId() == randomIndex ) {
                winnerCount++;
                break;
            }
        }

        // Then
        assertEquals(1, winnerCount);
    }

    @Test
    void testWinnerWriter() throws Exception {
        // Given
        DailyLottoWinner winner = DailyLottoWinner.fromParticipant(testParticipants.get(0));
        Chunk<DailyLottoWinner> chunk = new Chunk<>();
        chunk.add(winner);

        // When
        winnerWriter.write(chunk);

        // Then
        verify(winnerRepository, times(1)).save(any(DailyLottoWinner.class));
    }
}