package greensnail_backend.GreenSnail.service;

import greensnail_backend.GreenSnail.dto.DiagnosisRequestDto;
import greensnail_backend.GreenSnail.dto.DiagnosisResultDto;
import greensnail_backend.GreenSnail.dto.QuestionDto;
import greensnail_backend.GreenSnail.entity.AgeGroup;
import greensnail_backend.GreenSnail.entity.DiagnosisResult;
import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.repository.DiagnosisResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiagnosisService {

    private final DiagnosisResultRepository diagnosisResultRepository;

    public DiagnosisService(DiagnosisResultRepository diagnosisResultRepository) {
        this.diagnosisResultRepository = diagnosisResultRepository;
    }

    // 1. 연령대에 따라 질문 리스트 반환
    public List<QuestionDto> getQuestionsByAgeGroup(AgeGroup ageGroup) {
        return ageGroup == AgeGroup.UNDER_14 ? getUnder14Questions() : getOver14Questions();
    }

    // 2. 자가진단 점수 계산 및 결과 분류 + 결과 DB 저장
    public DiagnosisResultDto calculateResult(DiagnosisRequestDto requestDto, User user) {
        List<Integer> answers = requestDto.getAnswers();

        int score = answers.stream().mapToInt(Integer::intValue).sum();

        String resultType;
        if (score <= 4) {
            resultType = "일반군";
        } else if (score <= 9) {
            resultType = "경계선 지능 탐색군";
        } else {
            resultType = "경계선 지능 위험군";
        }

        // 진단 결과 DB에 저장 (사용자 포함)
        DiagnosisResult result = new DiagnosisResult(user, requestDto.getAgeGroup(), score, resultType);
        diagnosisResultRepository.save(result);

        return new DiagnosisResultDto(score, resultType);
    }

    // 3. 저장된 결과 전체 조회 (선택 기능: 마이페이지 등)
    public List<DiagnosisResult> getAllResults() {
        return diagnosisResultRepository.findAll();
    }

    private List<QuestionDto> getUnder14Questions() {
        return List.of(
                new QuestionDto(1, "긴 문장을 들으면 내용을 쉽게 이해하기 어려운가요?"),
                new QuestionDto(2, "단어의 의미를 설명하는 것이 어렵나요?"),
                new QuestionDto(3, "책을 읽을 때 내용을 이해하는 것이 힘든가요?"),
                new QuestionDto(4, "새로운 놀이 규칙을 배우는 것이 어렵나요?"),
                new QuestionDto(5, "블록 쌓기나 퍼즐 맞추기가 어렵나요?"),
                new QuestionDto(6, "여러 가지 지시를 한 번에 듣고 따라 하는 것이 어렵나요?"),
                new QuestionDto(7, "숫자나 짧은 문장을 들으면 금방 잊어버리나요?"),
                new QuestionDto(8, "순서를 정해놓고 해야 하는 일이 어려운가요?"),
                new QuestionDto(9, "다른 친구들보다 숙제나 공부를 끝내는 속도가 느린가요?"),
                new QuestionDto(10, "시간을 맞추거나 계획을 세우는 것이 어렵나요?")
        );
    }

    private List<QuestionDto> getOver14Questions() {
        return List.of(
                new QuestionDto(1, "긴 문장을 이해하는 것이 어렵나요?"),
                new QuestionDto(2, "단어의 의미를 설명하는 것이 어렵나요?"),
                new QuestionDto(3, "글을 읽을 때 내용을 이해하기 어렵나요?"),
                new QuestionDto(4, "새로운 규칙을 배우거나 적용하는 것이 어렵나요?"),
                new QuestionDto(5, "공간을 활용하는 일이 어렵나요?"),
                new QuestionDto(6, "복잡한 업무나 과제를 순서대로 처리하는 것이 힘든가요?"),
                new QuestionDto(7, "전화번호나 계좌번호 같은 숫자를 기억하는 것이 어렵나요?"),
                new QuestionDto(8, "여러 가지 일을 동시에 하면 헷갈리나요?"),
                new QuestionDto(9, "일을 처리하는 속도가 다른 사람들보다 느린가요?"),
                new QuestionDto(10, "시간을 맞추거나 계획을 세우는 것이 어렵나요?")
        );
    }
}
