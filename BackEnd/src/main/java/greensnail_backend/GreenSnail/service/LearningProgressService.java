package greensnail_backend.GreenSnail.service;

import greensnail_backend.GreenSnail.entity.LearningProgress;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.repository.LearningProgressRepository;
import greensnail_backend.GreenSnail.repository.SummaryRepository;
import greensnail_backend.GreenSnail.repository.WordLearningLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LearningProgressService {

    private final LearningProgressRepository learningProgressRepository;
    private final SummaryRepository summaryRepository;
    private final WordLearningLogRepository wordLearningLogRepository;

    @Transactional
    public void updateProgress(String providerId, LocalDate date) {
        // yearMonth 추출 및 유효성 검증
        String yearMonth = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
        int day = date.getDayOfMonth();

        // 단어 학습 및 문장 요약 평가
        boolean wordPassed = wordPassed(providerId, date);
        boolean summaryPassed = summaryPassed(providerId, date);
        int status = (wordPassed && summaryPassed) ? 2 : (wordPassed || summaryPassed) ? 1 : 0;

        // LearningProgress 조회 또는 생성
        Optional<LearningProgress> optionalProgress = learningProgressRepository.findByProviderIdAndMonthInfo(providerId, yearMonth);
        LearningProgress progress;
        if (optionalProgress.isPresent()) {
            progress = optionalProgress.get();
        } else {
            progress = new LearningProgress();
            progress.setProviderId(providerId);
            progress.setMonthInfo(yearMonth);
            progress.setProgress("00000000000000000000000000000000");
        }

        // progress 문자열 업데이트
        char[] progressArray = progress.getProgress().toCharArray();
        progressArray[day] = (char) (status + '0'); // status(0,1,2)를 문자로
        progress.setProgress(new String(progressArray));

        learningProgressRepository.save(progress);
    }

    public String getProgress(String providerId, LocalDate date) {
        // yearMonth 추출 및 유효성 검증
        String yearMonth = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
        try {
            YearMonth.parse(yearMonth);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "Invalid yearMonth format: " + yearMonth);
        }

        return learningProgressRepository.findByProviderIdAndMonthInfo(providerId, yearMonth)
                .map(LearningProgress::getProgress)
                .orElse("00000000000000000000000000000000");
    }

    private boolean wordPassed(String providerId, LocalDate date) {
        int solvedCount = wordLearningLogRepository.countByProviderIdAndSolvedAt(providerId, date);
        return solvedCount >= 10;
    }

    private boolean summaryPassed(String providerId, LocalDate date) {
        return summaryRepository.existsByProviderIdAndClearedAtDateAndIsClearedTrue(providerId, date);
    }
}