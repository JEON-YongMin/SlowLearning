package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.dto.*;
import greensnail_backend.GreenSnail.service.SummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/summaries")
@RequiredArgsConstructor
@Slf4j
public class SummaryController {

    private final SummaryService summaryService;

    @PostMapping
    public SummaryResponseDto submitSummary(
            @RequestHeader("Provider-Id") String providerId,
            @RequestBody SummaryRequestDto dto
    ) {
        log.info("POST /summaries - Provider-Id: {}, ArticleId: {}", providerId, dto != null ? dto.getArticleId() : "null");

        if (providerId == null || providerId.trim().isEmpty()) {
            log.error("Provider-Id header is missing or empty");
            throw new IllegalArgumentException("Provider-Id header is required");
        }

        if (dto == null) {
            log.error("Request body is null");
            throw new IllegalArgumentException("Request body is required");
        }

        if (dto.getArticleId() == null) {
            log.error("Article ID is null");
            throw new IllegalArgumentException("Article ID is required");
        }

        if (dto.getUserSummary() == null || dto.getUserSummary().trim().isEmpty()) {
            log.error("User summary is null or empty");
            throw new IllegalArgumentException("User summary is required");
        }

        SummaryResponseDto response = summaryService.submitSummary(providerId, dto);
        log.info("Successfully processed summary submission - Summary ID: {}", response.getSummaryId());

        return response;
    }

    @PutMapping("/{summaryId}/ai-feedback")
    public SummaryResponseDto updateAiFeedback(
            @PathVariable Long summaryId,
            @RequestBody AiFeedbackUpdateDto dto
    ) {
        log.info("PUT /summaries/{}/ai-feedback", summaryId);

        if (summaryId == null) {
            log.error("Summary ID is null");
            throw new IllegalArgumentException("Summary ID is required");
        }

        if (dto == null || dto.getAiFeedback() == null) {
            log.error("AI feedback is null");
            throw new IllegalArgumentException("AI feedback is required");
        }

        SummaryResponseDto response = summaryService.updateAiFeedback(summaryId, dto);
        log.info("Successfully updated AI feedback for summary: {}", summaryId);

        return response;
    }

    @GetMapping("/search")
    public List<SummarySearchResponse> searchSummarizedArticles(
            @RequestParam String keyword,
            @RequestHeader("Provider-Id") String providerId
    ) {
        log.info("GET /summaries/search - keyword: {}, Provider-Id: {}", keyword, providerId);

        if (providerId == null || providerId.trim().isEmpty()) {
            log.error("Provider-Id header is missing or empty");
            throw new IllegalArgumentException("Provider-Id header is required");
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            log.error("Keyword parameter is missing or empty");
            throw new IllegalArgumentException("Keyword parameter is required");
        }

        List<SummarySearchResponse> response = summaryService.searchSummarizedArticles(keyword, providerId);
        log.info("Successfully searched summaries - found {} results", response.size());

        return response;
    }

    @GetMapping("/summaries/all")
    public List<MySummaryDto> getAllMySummaries(@RequestParam String providerId) {
        log.info("GET /summaries/summaries/all - providerId: {}", providerId);

        if (providerId == null || providerId.trim().isEmpty()) {
            log.error("ProviderId parameter is missing or empty");
            throw new IllegalArgumentException("ProviderId parameter is required");
        }

        List<MySummaryDto> response = summaryService.getMySummaries(providerId);
        log.info("Successfully retrieved summaries - found {} results", response.size());

        return response;
    }
}