package greensnail_backend.GreenSnail.service;

import greensnail_backend.GreenSnail.entity.Diary;
import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;

    // 1. 일기 저장
    public Diary createDiary(User user, LocalDate date, String content) {
        // 같은 날짜에 이미 작성한 일기가 있다면 저장 불가
        diaryRepository.findByUserAndDate(user, date).ifPresent(d -> {
            throw new IllegalStateException("이미 해당 날짜에 작성된 일기가 있습니다.");
        });

        Diary diary = Diary.builder()
                .user(user)
                .date(date)
                .content(content)
                .build();

        return diaryRepository.save(diary);
    }

    // 2. AI 피드백 저장
    public Diary updateAiFeedback(User user, LocalDate date, String feedback) {
        Diary diary = diaryRepository.findByUserAndDate(user, date)
                .orElseThrow(() -> new NoSuchElementException("해당 날짜에 작성된 일기가 없습니다."));

        diary.updateAiFeedback(feedback);
        return diaryRepository.save(diary);
    }

    // 3. 특정 날짜 일기 조회
    public Diary getDiaryByDate(User user, LocalDate date) {
        return diaryRepository.findByUserAndDate(user, date)
                .orElseThrow(() -> new NoSuchElementException("해당 날짜의 일기가 존재하지 않습니다."));
    }
}
