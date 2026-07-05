package greensnail_backend.GreenSnail.service;

import greensnail_backend.GreenSnail.dto.WordQuizItem;
import greensnail_backend.GreenSnail.dto.WordQuizResult;
import greensnail_backend.GreenSnail.dto.QuizAnswerDto;
import greensnail_backend.GreenSnail.dto.WordQuizResultItem;
import greensnail_backend.GreenSnail.entity.Word;
import greensnail_backend.GreenSnail.entity.WordLearningLog;
import greensnail_backend.GreenSnail.entity.WordQuiz;
import greensnail_backend.GreenSnail.entity.WrongAnswerNote;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.repository.WordLearningLogRepository;
import greensnail_backend.GreenSnail.repository.WordQuizRepository;
import greensnail_backend.GreenSnail.repository.WordRepository;
import greensnail_backend.GreenSnail.repository.WrongAnswerNoteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordQuizService {

    private static final Logger logger = LoggerFactory.getLogger(WordQuizService.class);

    private final WordRepository wordRepository;
    private final WordLearningLogRepository learningLogRepository;
    private final WordQuizRepository wordQuizRepository;
    private final WrongAnswerNoteRepository wrongAnswerNoteRepository;

    private static final int MAX_WORD_SELECTION = 10;
    private static final int MAX_SAVE_ATTEMPTS = 20;
    private static final int MAX_QUIZ_PROCESSING = 50;

    @Transactional
    public WordQuizResult getWordsForDate(String providerId, LocalDate quizDate) {
        logger.info("🔍 [START] getWordsForDate - providerId: {}, quizDate: {}", providerId, quizDate);

        try {
            validateDateInput(quizDate);

            List<WordLearningLog> logs = learningLogRepository.findByProviderIdAndQuizDate(providerId, quizDate);
            logger.info("📊 기존 로그 수: {}", logs.size());

            if (logs.isEmpty()) {
                validateTodayAccess(quizDate);
                logs = createNewQuizSafely(providerId, quizDate);
            }

            boolean isSolved = logs.stream().allMatch(WordLearningLog::isSolved);
            logger.info("🎯 퀴즈 해결 상태: {}", isSolved);

            List<WordQuizItem> quizList = createQuizItemsSafely(logs);

            WordQuizResult result = WordQuizResult.builder()
                    .quizDate(quizDate)
                    .isSolved(isSolved)
                    .quizList(quizList)
                    .build();

            logger.info("✅ [SUCCESS] getWordsForDate 완료 - 퀴즈 수: {}", quizList.size());
            return result;

        } catch (CustomException e) {
            logger.error("❌ [CUSTOM_ERROR] getWordsForDate 실패 - providerId: {}, quizDate: {}, error: {}",
                    providerId, quizDate, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("❌ [SYSTEM_ERROR] getWordsForDate 실패 - providerId: {}, quizDate: {}, error: {}",
                    providerId, quizDate, e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "퀴즈 조회 중 오류가 발생했습니다.");
        }
    }

    private void validateDateInput(LocalDate quizDate) {
        LocalDate today = LocalDate.now();
        if (quizDate.isAfter(today)) {
            logger.warn("❌ 미래 날짜 요청: {}", quizDate);
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "미래 날짜의 단어는 조회할 수 없습니다.");
        }
    }

    private void validateTodayAccess(LocalDate quizDate) {
        LocalDate today = LocalDate.now();
        if (!quizDate.equals(today)) {
            logger.warn("❌ 과거 날짜에 기록 없음: {}", quizDate);
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND, "이 날짜에는 학습한 기록이 없습니다.");
        }
    }

    private List<WordLearningLog> createNewQuizSafely(String providerId, LocalDate quizDate) {
        logger.info("🆕 [START] 새 퀴즈 생성 시작");

        try {
            long totalWords = wordRepository.count();
            logger.info("📝 전체 단어 수: {}", totalWords);

            if (totalWords == 0) {
                logger.error("❌ 데이터베이스에 단어가 없음!");
                throw new CustomException(ErrorCode.ENTITY_NOT_FOUND, "데이터베이스에 단어가 없습니다.");
            }

            int selectCount = Math.min(MAX_WORD_SELECTION, (int) totalWords);
            logger.info("🎯 선택할 단어 수: {} (최대: {})", selectCount, MAX_WORD_SELECTION);

            List<Word> allWords = wordRepository.findAll();
            if (allWords.isEmpty()) {
                logger.error("❌ findAll() 결과가 비어있음!");
                throw new CustomException(ErrorCode.ENTITY_NOT_FOUND, "단어 조회 실패");
            }

            logger.info("📋 실제 조회된 단어 수: {}", allWords.size());

            long seed = providerId.hashCode() + quizDate.toEpochDay();
            Collections.shuffle(allWords, new Random(seed));
            List<Word> selectedWords = allWords.subList(0, selectCount);

            logger.info("✅ 단어 선택 완료: {} 개", selectedWords.size());

            return saveWordLogsSafely(providerId, quizDate, selectedWords);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("❌ [ERROR] 새 퀴즈 생성 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "새 퀴즈 생성 중 오류가 발생했습니다.");
        }
    }

    private List<WordLearningLog> saveWordLogsSafely(String providerId, LocalDate quizDate, List<Word> words) {
        logger.info("💾 [START] 단어 로그 저장 시작 - 대상: {}개", words.size());

        List<WordLearningLog> savedLogs = new ArrayList<>();
        int saveCount = 0;
        int errorCount = 0;

        for (Word word : words) {
            if (saveCount >= MAX_SAVE_ATTEMPTS) {
                logger.warn("⚠️ 최대 저장 시도 횟수 초과! saveCount: {}", saveCount);
                break;
            }

            try {
                WordLearningLog wordLog = WordLearningLog.builder()
                        .providerId(providerId)
                        .quizDate(quizDate)
                        .word(word)
                        .isSolved(false)
                        .build();

                WordLearningLog saved = learningLogRepository.save(wordLog);
                savedLogs.add(saved);
                saveCount++;

                logger.debug("💾 저장 성공: {} ({}번째)", word.getWord(), saveCount);

            } catch (Exception e) {
                errorCount++;
                logger.error("❌ 단어 저장 실패: {} - {} ({}번째 에러)",
                        word.getWord(), e.getMessage(), errorCount);

                if (errorCount >= 5) {
                    logger.error("❌ 너무 많은 저장 에러 발생! 중단합니다.");
                    break;
                }
            }
        }

        logger.info("✅ [SUCCESS] 단어 로그 저장 완료 - 성공: {}개, 실패: {}개", saveCount, errorCount);

        if (savedLogs.isEmpty()) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "모든 단어 저장에 실패했습니다.");
        }

        return learningLogRepository.findByProviderIdAndQuizDate(providerId, quizDate);
    }

    private List<WordQuizItem> createQuizItemsSafely(List<WordLearningLog> logs) {
        logger.info("🎮 [START] 퀴즈 아이템 생성 시작 - 대상: {}개", logs.size());

        List<WordQuizItem> quizItems = new ArrayList<>();
        int processCount = 0;
        int errorCount = 0;

        for (WordLearningLog log : logs) {
            if (processCount >= MAX_QUIZ_PROCESSING) {
                logger.warn("⚠️ 최대 퀴즈 처리 횟수 초과! processCount: {}", processCount);
                break;
            }

            try {
                Word word = log.getWord();
                if (word == null) {
                    logger.warn("⚠️ WordLearningLog의 Word가 null임: logId={}", log.getId());
                    continue;
                }

                Optional<WordQuiz> quizOpt = wordQuizRepository.findByWord(word);
                if (quizOpt.isEmpty()) {
                    logger.warn("⚠️ 퀴즈가 없는 단어: {} (wordId: {})", word.getWord(), word.getId());
                    errorCount++;
                    continue;
                }

                WordQuiz quiz = quizOpt.get();

                WordQuizItem item = WordQuizItem.builder()
                        .wordId(word.getId())
                        .quizId(quiz.getId())
                        .word(word.getWord())
                        .easyDefinition(word.getEasyDefinition())
                        .example(word.getExample())
                        .dictionaryDefinition(word.getDictionaryDefinition())
                        .question(quiz.getQuestion())
                        .choices(new ArrayList<>(quiz.getChoices())) // 방어적 복사
                        .answer(quiz.getAnswer())
                        .explanation(quiz.getExplanation())
                        .build();

                quizItems.add(item);
                processCount++;

                logger.debug("🎮 퀴즈 아이템 생성 성공: {} ({}번째)", word.getWord(), processCount);

            } catch (Exception e) {
                errorCount++;
                logger.error("❌ 퀴즈 아이템 생성 실패: {} ({}번째 에러)", e.getMessage(), errorCount);

                if (errorCount >= 10) {
                    logger.error("❌ 너무 많은 퀴즈 생성 에러 발생! 중단합니다.");
                    break;
                }
            }
        }

        logger.info("✅ [SUCCESS] 퀴즈 아이템 생성 완료 - 성공: {}개, 실패: {}개", processCount, errorCount);

        if (quizItems.isEmpty()) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "모든 퀴즈 아이템 생성에 실패했습니다.");
        }

        return quizItems;
    }

    @Transactional
    public void markQuizAsSolved(String providerId, LocalDate quizDate) {
        logger.info("🎯 [START] markQuizAsSolved - providerId: {}, quizDate: {}", providerId, quizDate);

        try {
            List<WordLearningLog> logs = learningLogRepository.findByProviderIdAndQuizDate(providerId, quizDate);

            if (logs.isEmpty()) {
                logger.warn("❌ 해당 날짜에 퀴즈 없음: {}", quizDate);
                throw new CustomException(ErrorCode.ENTITY_NOT_FOUND, "해당 날짜에 제공된 퀴즈가 없습니다.");
            }

            if (logs.stream().allMatch(WordLearningLog::isSolved)) {
                logger.warn("⚠️ 이미 푼 퀴즈: {}", quizDate);
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 푼 퀴즈입니다.");
            }

            logs.forEach(log -> {
                log.setSolved(true);
                log.setSolvedAt(LocalDate.now());
            });

            learningLogRepository.saveAll(logs);
            logger.info("✅ [SUCCESS] 퀴즈 해결 표시 완료 - 개수: {}", logs.size());

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("❌ [ERROR] markQuizAsSolved 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "퀴즈 해결 표시 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public List<WordQuizResultItem> checkQuizAnswers(String providerId, LocalDate date, List<QuizAnswerDto> answers) {
        logger.info("📝 [START] checkQuizAnswers - providerId: {}, date: {}, answers: {}개",
                providerId, date, answers.size());

        try {
            if (answers.size() > MAX_QUIZ_PROCESSING) {
                logger.warn("⚠️ 너무 많은 답안: {}개 (최대: {}개)", answers.size(), MAX_QUIZ_PROCESSING);
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "답안 수가 너무 많습니다.");
            }

            List<WordQuizResultItem> resultList = new ArrayList<>();
            int processCount = 0;

            for (QuizAnswerDto answer : answers) {
                if (processCount >= MAX_QUIZ_PROCESSING) {
                    logger.warn("⚠️ 최대 답안 처리 횟수 초과!");
                    break;
                }

                try {
                    WordQuiz quiz = wordQuizRepository.findById(answer.getQuizId())
                            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "퀴즈 ID 오류: " + answer.getQuizId()));

                    boolean correct = quiz.getAnswer().equals(answer.getSelectedAnswer());

                    resultList.add(WordQuizResultItem.of(
                            quiz.getId(),
                            quiz.getWord().getWord(),
                            answer.getSelectedAnswer(),
                            quiz.getAnswer(),
                            quiz.getExplanation()
                    ));

                    updateLearningLogSafely(providerId, quiz.getWord().getId());

                    if (!correct) {
                        saveWrongAnswerSafely(providerId, quiz.getWord(), date);
                    }

                    processCount++;
                    logger.debug("📝 답안 처리 완료: {} ({}번째)", quiz.getWord().getWord(), processCount);

                } catch (CustomException e) {
                    throw e;
                } catch (Exception e) {
                    logger.error("❌ 답안 처리 실패: {}", e.getMessage());
                }
            }

            logger.info("✅ [SUCCESS] checkQuizAnswers 완료 - 처리: {}개", resultList.size());
            return resultList;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("❌ [ERROR] checkQuizAnswers 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "답안 확인 중 오류가 발생했습니다.");
        }
    }

    private void updateLearningLogSafely(String providerId, Long wordId) {
        try {
            learningLogRepository.findByProviderIdAndWordId(providerId, wordId)
                    .ifPresentOrElse(
                            log -> {
                                log.setSolved(true);
                                log.setSolvedAt(LocalDate.now());
                                learningLogRepository.save(log);
                                logger.debug("📝 학습 로그 업데이트 성공: wordId={}", wordId);
                            },
                            () -> logger.warn("⚠️ 학습 로그 없음: providerId={}, wordId={}", providerId, wordId)
                    );
        } catch (Exception e) {
            logger.error("❌ 학습 로그 업데이트 실패: providerId={}, wordId={}, error={}",
                    providerId, wordId, e.getMessage());
        }
    }

    private void saveWrongAnswerSafely(String providerId, Word word, LocalDate date) {
        try {
            if (!wrongAnswerNoteRepository.existsByProviderIdAndWord(providerId, word)) {
                wrongAnswerNoteRepository.save(WrongAnswerNote.builder()
                        .providerId(providerId)
                        .word(word)
                        .quizDate(date)
                        .isSolved(false)
                        .build());
                logger.debug("📝 틀린 답안 저장 성공: {}", word.getWord());
            }
        } catch (Exception e) {
            logger.error("❌ 틀린 답안 저장 실패: word={}, error={}", word.getWord(), e.getMessage());
        }
    }

    public List<WordQuizItem> getWrongAnswerNotes(String providerId) {
        logger.info("📚 [START] getWrongAnswerNotes - providerId: {}", providerId);

        try {
            List<WrongAnswerNote> notes = wrongAnswerNoteRepository.findByProviderIdAndIsSolvedFalse(providerId);
            logger.info("📊 틀린 답안 노트 수: {}", notes.size());

            List<WordQuizItem> items = notes.stream()
                    .limit(MAX_QUIZ_PROCESSING) // 무한루프 방지
                    .map(note -> {
                        Word word = note.getWord();
                        WordQuiz quiz = wordQuizRepository.findByWord(word)
                                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "퀴즈가 없습니다: " + word.getWord()));

                        return WordQuizItem.builder()
                                .wordId(word.getId())
                                .quizId(quiz.getId())
                                .word(word.getWord())
                                .easyDefinition(word.getEasyDefinition())
                                .example(word.getExample())
                                .dictionaryDefinition(word.getDictionaryDefinition())
                                .question(quiz.getQuestion())
                                .choices(new ArrayList<>(quiz.getChoices()))
                                .answer(quiz.getAnswer())
                                .explanation(null)
                                .build();
                    })
                    .collect(Collectors.toList());

            logger.info("✅ [SUCCESS] getWrongAnswerNotes 완료 - 개수: {}", items.size());
            return items;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("❌ [ERROR] getWrongAnswerNotes 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "틀린 답안 노트 조회 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public WordQuizResultItem submitWrongNoteAnswer(String providerId, QuizAnswerDto answer) {
        logger.info("🔄 [START] submitWrongNoteAnswer - providerId: {}, quizId: {}",
                providerId, answer.getQuizId());

        try {
            WordQuiz quiz = wordQuizRepository.findById(answer.getQuizId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "퀴즈 ID 오류: " + answer.getQuizId()));

            boolean isCorrect = quiz.getAnswer().equals(answer.getSelectedAnswer());

            if (isCorrect) {
                wrongAnswerNoteRepository.findByProviderIdAndWordId(providerId, quiz.getWord().getId())
                        .ifPresent(note -> {
                            note.setSolved(true);
                            wrongAnswerNoteRepository.save(note);
                            logger.info("✅ 틀린 답안 노트 해결: wordId={}", quiz.getWord().getId());
                        });
            }

            WordQuizResultItem result = WordQuizResultItem.of(
                    quiz.getId(),
                    quiz.getWord().getWord(),
                    answer.getSelectedAnswer(),
                    quiz.getAnswer(),
                    quiz.getExplanation()
            );

            logger.info("✅ [SUCCESS] submitWrongNoteAnswer 완료 - correct: {}", isCorrect);
            return result;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("❌ [ERROR] submitWrongNoteAnswer 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "틀린 답안 제출 중 오류가 발생했습니다.");
        }
    }
}