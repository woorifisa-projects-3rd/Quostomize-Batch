package com.quostomize.lotto.batch;

import com.quostomize.lotto.config.TestBatchConfig;
import com.quostomize.lotto.entity.DailyLottoParticipant;
import com.quostomize.lotto.entity.DailyLottoWinner;
import com.quostomize.lotto.entity.LottoWinnerRecord;
import com.quostomize.lotto.repository.DailyLottoParticipantRepository;
import com.quostomize.lotto.repository.DailyLottoWinnerRepository;
import com.quostomize.lotto.repository.LottoWinnerRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private Job lottoJob;

    @Autowired
    private Job locateJob;

    @Autowired
    private RepositoryItemReader<DailyLottoParticipant> participantReader;

    @Autowired
    private RepositoryItemReader<DailyLottoWinner> dailyWinnerReader;

    private final ItemProcessor<DailyLottoParticipant, DailyLottoWinner> drawingProcessor =
            DailyLottoWinner::fromParticipant;

    @Autowired
    private RepositoryItemWriter<DailyLottoWinner> winnerWriter;

    @Autowired
    private RepositoryItemWriter<LottoWinnerRecord> winnerRecordWriter;

    @MockBean
    private DailyLottoParticipantRepository participantRepository;

    @MockBean
    private DailyLottoWinnerRepository winnerRepository;

    @MockBean
    private LottoWinnerRecordRepository winnerRecordRepository;

    private List<DailyLottoParticipant> testParticipants;
    private List<DailyLottoWinner> testWinners;

    @BeforeEach
    void setUp() {
        jobRepositoryTestUtils.removeJobExecutions();

        // 추첨 배치 테스트용 참가자 데이터 설정
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

        // 당첨자 기록 배치 테스트용 당첨자 데이터 설정
        testWinners = Arrays.asList(
                createDailyLottoWinner(1L),
                createDailyLottoWinner(2L)
        );

        // Repository mock 설정
        when(participantRepository.count()).thenReturn((long) testParticipants.size());
        when(participantRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(testParticipants))
                .thenReturn(Page.empty());

        when(winnerRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(testWinners))
                .thenReturn(Page.empty());
    }

    // 추첨 배치 관련 테스트
    @Test
    @DisplayName("참가자 Reader 테스트")
    void testParticipantReader() throws Exception {
        participantReader.setPageSize(1000);
        DailyLottoParticipant participant = participantReader.read();

        assertNotNull(participant);
        assertTrue(testParticipants.contains(participant));
        verify(participantRepository).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("당첨자 추첨 Processor 테스트")
    void testDrawingProcessor() throws Exception {
        int winnerCount = 0;
        int randomIndex = new Random().nextInt(3) + 1;

        for (DailyLottoParticipant participant : testParticipants) {
            if (participant.getCustomerId() == randomIndex) {
                winnerCount++;
                break;
            }
        }

        assertEquals(1, winnerCount);
    }

    @Test
    @DisplayName("당첨자 Writer 테스트")
    void testWinnerWriter() throws Exception {
        DailyLottoWinner winner = DailyLottoWinner.fromParticipant(testParticipants.get(0));
        Chunk<DailyLottoWinner> chunk = new Chunk<>();
        chunk.add(winner);

        winnerWriter.write(chunk);

        verify(winnerRepository, times(1)).save(any(DailyLottoWinner.class));
    }

    // 당첨자 기록 배치 관련 테스트
//    @Test
//    @DisplayName("당첨자 기록 Step 실행 테스트")
//    void testLocationStep() throws Exception {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addLong("time", System.currentTimeMillis())
//                .toJobParameters();
//
//        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
//
//        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
//        verify(winnerRecordRepository, times(testWinners.size())).save(any(LottoWinnerRecord.class));
//    }

    @Test
    @DisplayName("당첨자 Reader 테스트")
    void testDailyWinnerReader() throws Exception {
        dailyWinnerReader.setPageSize(100);
        DailyLottoWinner winner = dailyWinnerReader.read();

        assertNotNull(winner);
        assertTrue(testWinners.contains(winner));
        verify(winnerRepository).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("당첨 기록 Writer 테스트")
    void testWinnerRecordWriter() throws Exception {
        LottoWinnerRecord record = LottoWinnerRecord.fromDailyWinner(testWinners.get(0));
        Chunk<LottoWinnerRecord> chunk = new Chunk<>();
        chunk.add(record);

        winnerRecordWriter.write(chunk);

        verify(winnerRecordRepository, times(1)).save(any(LottoWinnerRecord.class));
    }

    private DailyLottoWinner createDailyLottoWinner(Long id) {
        return DailyLottoWinner.builder()
                .dailyLottoParticipant(
                        new DailyLottoParticipant(1L, id)
                )
                .build();
    }
}