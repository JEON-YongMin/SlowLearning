package greensnail_backend.GreenSnail.service;

import greensnail_backend.GreenSnail.dto.*;
import greensnail_backend.GreenSnail.entity.Article;
import greensnail_backend.GreenSnail.entity.Summary;
import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.repository.ArticleRepository;
import greensnail_backend.GreenSnail.repository.SummaryRepository;
import greensnail_backend.GreenSnail.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SummaryService {

    private final SummaryRepository summaryRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional
    public SummaryResponseDto submitSummary(String providerId, SummaryRequestDto dto) {
        log.info("Summary submission started - providerId: {}, articleId: {}", providerId, dto.getArticleId());

        if (providerId == null || providerId.trim().isEmpty()) {
            log.error("Provider ID is null or empty");
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (dto.getArticleId() == null) {
            log.error("Article ID is null");
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (dto.getUserSummary() == null || dto.getUserSummary().trim().isEmpty()) {
            log.error("User summary is null or empty");
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> {
                    log.error("User not found for providerId: {}", providerId);
                    return new CustomException(ErrorCode.USER_NOT_FOUND);
                });

        Article article = articleRepository.findById(dto.getArticleId())
                .orElseThrow(() -> {
                    log.error("Article not found for articleId: {}", dto.getArticleId());
                    return new CustomException(ErrorCode.ARTICLE_NOT_FOUND);
                });

        try {
            Summary summary = Summary.create(user, article, dto.getUserSummary(), null);
            Summary savedSummary = summaryRepository.save(summary);
            log.info("Successfully saved new summary: ID={}", savedSummary.getId());
            return SummaryResponseDto.from(savedSummary);
        } catch (Exception e) {
            log.error("Unexpected error while creating summary", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public SummaryResponseDto updateAiFeedback(Long summaryId, AiFeedbackUpdateDto dto) {
        log.info("Updating AI feedback for summary: {}", summaryId);

        Summary summary = summaryRepository.findById(summaryId)
                .orElseThrow(() -> {
                    log.error("Summary not found for ID: {}", summaryId);
                    return new CustomException(ErrorCode.SUMMARY_NOT_FOUND);
                });

        summary.updateAiFeedback(dto.getAiFeedback());
        summary.setCleared(true);
        summary.setClearedAt(LocalDate.now());

        try {
            Summary savedSummary = summaryRepository.save(summary);
            log.info("Successfully updated AI feedback for summary: {}", summaryId);
            return SummaryResponseDto.from(savedSummary);
        } catch (Exception e) {
            log.error("Error updating AI feedback for summary: {}", summaryId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public List<SummarySearchResponse> searchSummarizedArticles(String keyword, String providerId) {
        log.info("Searching summarized articles - keyword: {}, providerId: {}", keyword, providerId);

        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        if (providerId == null || providerId.trim().isEmpty()) {
            return List.of();
        }

        try {
            return summaryRepository.searchByKeywordAndProviderId(providerId, keyword);
        } catch (Exception e) {
            log.error("Error searching summarized articles", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public List<MySummaryDto> getMySummaries(String providerId) {
        log.info("Getting summaries for providerId: {}", providerId);

        if (providerId == null || providerId.trim().isEmpty()) {
            return List.of();
        }

        try {
            List<Summary> summaries = summaryRepository.findAllByProviderIdOrderByCreatedAtDesc(providerId);
            return summaries.stream().map(MySummaryDto::from).toList();
        } catch (Exception e) {
            log.error("Error getting summaries for providerId: {}", providerId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
